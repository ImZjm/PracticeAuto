package imzjm.practice.event;

import imzjm.practice.Data.UserAccount;
import imzjm.practice.kfc.ClockInfo;
import imzjm.practice.kfc.iKun;
import imzjm.practice.service.cdvisor;
import net.mamoe.mirai.event.*;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupMsgEvent extends SimpleListenerHost {
    private static final Map<Long, String> clockInProcessMap = new HashMap<>();

    @EventHandler
    public void cmd_class(GroupMessageEvent event) {
        String msg = event.getMessage().contentToString();
        switch (msg) {

            case "实习签到" -> {
                if (clockInProcessMap.containsKey(event.getSender().getId()))
                    break;
                new Thread(() -> clockInProcess(event)).start();
            }

            case "绑定账号" -> event.getGroup().sendMessage("请私信发送该指令!");
        }
    }


    private void clockInProcess(GroupMessageEvent e) {
        System.out.println("开始执行签到操作");
        UserAccount userAccount = UserAccount.INSTANCE;
        if (!userAccount.qq.get().containsKey(String.valueOf(e.getSender().getId()))) {
            e.getGroup().sendMessage("请先绑定账号!");
            e.getGroup().sendMessage("私信发送指令: 绑定账号");
            return;
        }

        Map<String, Map<String, String>> thisQQData = userAccount.qq.get().get(String.valueOf(e.getSender().getId()));

        //监听器过滤
        EventChannel<Event> channel = GlobalEventChannel.INSTANCE.filter(event -> ((MessageEvent) event).getSender().getId() == e.getSender().getId());

        if (thisQQData.size() == 0) {
            e.getGroup().sendMessage("请先绑定账号!");
            e.getGroup().sendMessage("私信发送指令: 绑定账号");

        } else if (thisQQData.size() >= 2) {
            List<String> bindAccount = new ArrayList<>();
            thisQQData.forEach((userId, userData) -> bindAccount.add(userId));

            e.getGroup().sendMessage("当前已绑定账号: " + bindAccount);
            e.getGroup().sendMessage("请发送要签到的账号!");

            channel.subscribeOnce(GroupMessageEvent.class, event -> {
                if (bindAccount.contains(event.getMessage().contentToString())) {
                    event.getGroup().sendMessage("选定成功: " + event.getMessage().contentToString());

                    clockInProcessMap.put(event.getSender().getId(), event.getMessage().contentToString());

                    clockIn(e,channel,thisQQData);
                }
                else
                    event.getGroup().sendMessage("失败, 请重试");
            });
        } else {
            thisQQData.forEach((userId, userData) -> clockInProcessMap.put(e.getSender().getId(), userId));
            clockIn(e,channel,thisQQData);
        }

    }

    private void clockIn(GroupMessageEvent e, EventChannel<Event> channel, Map<String, Map<String, String>> thisQQData) {
        //签到信息
        ClockInfo clockInfo = new ClockInfo();

        if (thisQQData.get(clockInProcessMap.get(e.getSender().getId())).get("经度") != null && !e.getMessage().contentToString().equals("更新签到地址")) {
            clockInfo.setJd(thisQQData.get(clockInProcessMap.get(e.getSender().getId())).get("经度"));
            clockInfo.setWd(thisQQData.get(clockInProcessMap.get(e.getSender().getId())).get("纬度"));
            clockInfo.setLocation(thisQQData.get(clockInProcessMap.get(e.getSender().getId())).get("地址"));

            //直接向用户请求图片
            e.getGroup().sendMessage("签到地址:\n" + clockInfo.getLocation());
            e.getGroup().sendMessage("请发送签到图片");
        }
        else {

            e.getGroup().sendMessage("开始配置签到地点!\n获取经纬度信息: https://api.map.baidu.com/lbsapi/getpoint/index.html");
            e.getGroup().sendMessage("请发送经度");
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        //获取签到信息，并进行签到
        Listener<GroupMessageEvent> listener = channel.subscribe(GroupMessageEvent.class, event -> {
            //获取经度
            if (clockInfo.getJd() == null) {
                //判断经度合法性
                try {
                    double jd = Double.parseDouble(event.getMessage().contentToString());
                    if (jd < -180 || jd > 180) {
                        event.getGroup().sendMessage("经度不合法，请重新发送");
                        return ListeningStatus.LISTENING;
                    }
                } catch (NumberFormatException exception) {
                    event.getGroup().sendMessage("发送内容不合法");
                    return ListeningStatus.LISTENING;
                }

                //经度合法
                clockInfo.setJd(event.getMessage().contentToString());

                //提示用户接下来发送纬度
                event.getGroup().sendMessage("请发送纬度");
                return ListeningStatus.LISTENING;
            }

            //获取纬度
            else if (clockInfo.getWd() == null) {

                try {
                    double wd = Double.parseDouble(event.getMessage().contentToString());
                    if (wd < -90 || wd > 90) {
                        event.getGroup().sendMessage("纬度不合法，请重新发送");
                        return ListeningStatus.LISTENING;
                    }
                } catch (NumberFormatException exception) {
                    event.getGroup().sendMessage("发送内容不合法");
                    return ListeningStatus.LISTENING;
                }

                //纬度合法
                clockInfo.setWd(event.getMessage().contentToString());

                //提示用户, 接下来发送签到图片
                event.getGroup().sendMessage("请发送签到图片");
                return ListeningStatus.LISTENING;
            }

            //签到图片
            else if (clockInfo.getFilenames() == null) {

                Matcher matcher = Pattern.compile("(?<=mirai:image:)([^]]*)").matcher(event.getMessage().serializeToMiraiCode());
                String imageBase64;

                if (matcher.find()) {
                    //获取图片Url
                    String imgId = matcher.group();
                    Image image = Image.fromId(imgId);
                    String imageUrl = Image.queryUrl(image);
                    System.out.println("签到图片" + imageUrl);

                    //获取图片字节流
                    HttpClient httpClient = HttpClient.newHttpClient();
                    try {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(new URI(imageUrl))
                                .GET()
                                .build();

                        HttpResponse<byte[]> bytes = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
                        imageBase64 = Base64.getEncoder().encodeToString(bytes.body());

                    } catch (URISyntaxException | IOException | InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }

                }

                else {
                    event.getGroup().sendMessage("请发送图片");
                    return ListeningStatus.LISTENING;
                }

                //设置签到图片
                clockInfo.setFilenames("data:image/jpeg;base64," + imageBase64);

            }

            System.out.println("签到账号: " + clockInProcessMap.get(event.getSender().getId()));

            //开始准备签到数据
            //根据经纬度,更新详细位置信息
            //先存下用户输入的经纬度信息
            String userJd = clockInfo.getJd();
            String userWd = clockInfo.getWd();

            //通过调用api，更新详细经纬度等地理信息
            cdvisor.getClockInfo(clockInfo);
            System.out.println(clockInfo);

            //签到需要cookie
            iKun iKun = new iKun();
            iKun.setId(clockInProcessMap.get(event.getSender().getId()));
            iKun.setPwd(thisQQData.get(iKun.getId()).get("密码"));
            String cookie = cdvisor.getCookie(iKun, cdvisor.getToken());
            if (cookie == null || cookie.equals(""))
                event.getGroup().sendMessage("账号有误,请尝试重新绑定!");
            else {
                //信息准备完毕,开始签到
                String s = cdvisor.qdSaveNew(clockInfo, cookie);
                if (s.contains("1")) {
                    System.out.println("签到成功!");
                    event.getGroup().sendMessage(new At(event.getSender().getId()).plus("签到成功!"));
                    event.getGroup().sendMessage("签到地点: \n" + clockInfo.getLocation());

                    //签到成功，保存地址信息
                    thisQQData.get(iKun.getId()).put("经度", userJd);
                    thisQQData.get(iKun.getId()).put("纬度", userWd);
                    thisQQData.get(iKun.getId()).put("地址", clockInfo.getLocation());
                } else if (s.contains("2")) {
                    System.out.println("签到失败!");
                    event.getGroup().sendMessage("签到失败!");
                } else if (s.contains("3")) {
                    System.out.println("签到失败!");
                    event.getGroup().sendMessage("签到失败, 一天只能签到一次!");
                    thisQQData.get(iKun.getId()).put("经度", userJd);
                    thisQQData.get(iKun.getId()).put("纬度", userWd);
                    thisQQData.get(iKun.getId()).put("地址", clockInfo.getLocation());
                }
            }

            //结束签到会话
            clockInProcessMap.remove(event.getSender().getId());
            executor.shutdownNow();

            return ListeningStatus.STOPPED;
        });

        executor.schedule(
                () -> {
                    clockInProcessMap.remove(e.getSender().getId());
                    System.out.println(e.getSender().getId() + " - 长时间未操作 会话结束");
                    listener.complete();
                },
                5,
                TimeUnit.MINUTES
        );


    }
}
