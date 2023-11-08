package imzjm.practice;

import imzjm.practice.Data.UserAccount;
import imzjm.practice.event.AccountBindMsgEvent;
import imzjm.practice.event.GroupMsgEvent;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;

public final class PracticeAuto extends JavaPlugin {

    
/*                  欢迎来到屎山!!!

                       _ooOoo_
                      o8888888o
                      88" . "88
                      (| -_- |)
                      O\  =  /O
                   ____/`---'\____
                 .'  \\|     |//  `.
                /  \\|||  :  |||//  \
               /  _||||| -:- |||||-  \
               |   | \\\  -  /// |   |
               | \_|  ''\-/''  |   |
               \  .-\__  `-`  ___/-. /
             ___`. .'  /-.-\  `. . __
          ."" '<  `.___\_<|>_/___.'  >'"".
         | | :  `- \`.;`\ _ /`;.`/ - ` : | |
         \  \ `-.   \_ __\ /__ _/   .-` /  /
    ======`-.____`-.___\_____/___.-`____.-'======
                       `=-='

*/


    public static final PracticeAuto INSTANCE = new PracticeAuto();

    private PracticeAuto() {
        super(new JvmPluginDescriptionBuilder("imzjm.practice-auto", "0.1.0")
                .name("实习自动化")
                .info("武汉新宏博科技有限公司 - 云实习助理自动化交互 https://www.cdvisor.com:8443/")
                .author("恨别鸟惊心_")
                .build());
    }

    @Override
    public void onEnable() {
        getLogger().info("实习自动化 插件启动成功!!!");


        //注册监听事件
//        GlobalEventChannel.INSTANCE.registerListenerHost(new TestEvent());
        GlobalEventChannel.INSTANCE.registerListenerHost(new AccountBindMsgEvent());
        GlobalEventChannel.INSTANCE.registerListenerHost(new GroupMsgEvent());

        //读取文件
        this.reloadPluginData(UserAccount.INSTANCE);
    }
}
