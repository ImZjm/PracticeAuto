package imzjm.practice.event;

import imzjm.practice.Data.UserAccount;
import imzjm.practice.kfc.iKun;
import imzjm.practice.service.cdvisor;
import net.mamoe.mirai.event.*;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AccountBindMsgEvent extends SimpleListenerHost {
    //绑定会话列表
    private static final List<Long> bindProcessList = new ArrayList<>();

    @EventHandler
    public void cmd_class(MessageEvent event) {
        String msg = event.getMessage().contentToString();
        switch (msg) {
            case "绑定账号" -> {
                if (bindProcessList.contains(event.getSender().getId()))
                    break;
                if (event instanceof GroupMessageEvent) {
                    System.out.println("绑定账号指令不希望群聊触发");
                    break;
                }
                bindProcessList.add(event.getSender().getId());
                System.out.println("绑定账号");
                event.getSender().sendMessage("请发送学号");
                new Thread(() -> bindProcess(event)).start();
            }

            case "???" -> System.out.println("???");
        }
    }

    //创建一个”账号绑定“会话，处理来自同一个qq的信息
    private void bindProcess(MessageEvent e) {
        iKun iKun = new iKun();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        EventChannel<Event> channel = GlobalEventChannel.INSTANCE.filter(event -> ((MessageEvent) event).getSender().getId() == e.getSender().getId());
        Listener<MessageEvent> listener = channel.subscribe(MessageEvent.class, event -> {
            if (iKun.getId() == null) {
                iKun.setId(event.getMessage().contentToString());
                event.getSender().sendMessage("请发送密码");
            }

            else {
                System.out.println("开始设置密码");
                iKun.setPwd(event.getMessage().contentToString());

                //绑定会话结束
                bindProcessList.remove(e.getSender().getId());
                executor.shutdownNow();
                System.out.println("--------绑定会话结束");

                //尝试登录实习平台 获取Cookie
                String cookie = cdvisor.getCookie(iKun, cdvisor.getToken());
                System.out.println(cookie);

                if (!cookie.equals("")) {
                    event.getSender().sendMessage("绑定成功!");

                    //将账号和密码 写入配置文件
                    System.out.println("准备写入数据...");
                    UserAccount userAccount = UserAccount.INSTANCE;

                    if (userAccount.qq.get().get(String.valueOf(event.getSender().getId())) == null) {

                        userAccount.qq.get().put(String.valueOf(event.getSender().getId()), new HashMap<>() {{
                            put(iKun.getId(), new HashMap<>() {{
                                put("密码", iKun.getPwd());
                            }});
                        }});
                    }
                    userAccount.qq.get().get(String.valueOf(event.getSender().getId()))
                            .put(iKun.getId(), new HashMap<>() {{
                                put("密码", iKun.getPwd());
                            }});

                }
                else
                    event.getSender().sendMessage("绑定失败，请检查账号或密码!");

                //绑定成功 或 失败，都结束监听
                return ListeningStatus.STOPPED;
            }
            return ListeningStatus.LISTENING;
        });

        if (e.getMessage().contentToString().equals("绑定账号"))
            executor.schedule(
                    () -> {
                        bindProcessList.remove(e.getSender().getId());
                        e.getSender().sendMessage("5分钟未操作，已自动结束会话!");
                        listener.complete();
                    },
                    5,
                    TimeUnit.MINUTES);
    }

}
