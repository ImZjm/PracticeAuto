package imzjm.practice.event;

import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Image;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestEvent extends SimpleListenerHost {
    @EventHandler
    public void test(GroupMessageEvent event) {
        System.out.println("接收到信息");

        System.out.println(event.getMessage().toString());
        System.out.println(event.getMessage().contentToString());
        System.out.println(event.getMessage().serializeToMiraiCode());

        System.out.println("\n----------------\n");


        Matcher matcher = Pattern.compile("(?<=mirai:image:)([^]]*)").matcher(event.getMessage().serializeToMiraiCode());
        if (matcher.find()) {
            String imgId = matcher.group();
            System.out.println("imageId: " + imgId);
            Image image = Image.fromId(imgId);
            System.out.println(Image.queryUrl(image));

        }


    }

}
