package imzjm.practice.Data;

import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class UserAccount extends JavaAutoSavePluginData {

    public static final UserAccount INSTANCE = new UserAccount("students");

    public UserAccount(@NotNull String saveName) {
        super(saveName);
    }

    public final Value<Map<String, Map<String, Map<String, String>>>> qq = typedValue("user",
            createKType(Map.class, createKType(String.class),
                    createKType(Map.class, createKType(String.class),
                            createKType(Map.class, createKType(String.class),createKType(String.class)))),
            new HashMap<>() {{
                put("QQ号", new HashMap<>() {{
                    put("学号", new HashMap<>() {{
                        put("密码", "这里是密码!");
                        put("其他数据", "这里是其他数据!");
                    }});
                }});
            }}

    );
}
