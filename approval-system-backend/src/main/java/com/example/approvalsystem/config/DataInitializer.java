package com.example.approvalsystem.config;

import com.example.approvalsystem.entity.User;
import com.example.approvalsystem.store.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private InMemoryDataStore dataStore;

    @Override
    public void run(String... args) throws Exception {
        initUsers();
    }

    private void initUsers() {
        if (!dataStore.getAllUsers().isEmpty()) {
            return;
        }

        User zongjingli = User.builder()
                .userId(dataStore.generateId())
                .name("张总")
                .department("总经理办公室")
                .role("总经理")
                .directManagerId(null)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        dataStore.saveUser(zongjingli);

        User xiaoshouZhuguan = User.builder()
                .userId(dataStore.generateId())
                .name("李经理")
                .department("销售部")
                .role("部门主管")
                .directManagerId(zongjingli.getUserId())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        dataStore.saveUser(xiaoshouZhuguan);

        User xiaoshouYuangong1 = User.builder()
                .userId(dataStore.generateId())
                .name("小王")
                .department("销售部")
                .role("普通员工")
                .directManagerId(xiaoshouZhuguan.getUserId())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        dataStore.saveUser(xiaoshouYuangong1);

        User xiaoshouYuangong2 = User.builder()
                .userId(dataStore.generateId())
                .name("小张")
                .department("销售部")
                .role("普通员工")
                .directManagerId(xiaoshouZhuguan.getUserId())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        dataStore.saveUser(xiaoshouYuangong2);

        User caiwuZhuguan = User.builder()
                .userId(dataStore.generateId())
                .name("王财务")
                .department("财务部")
                .role("财务")
                .directManagerId(zongjingli.getUserId())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        dataStore.saveUser(caiwuZhuguan);

        System.out.println("========================================");
        System.out.println("  默认测试用户已创建：");
        System.out.println("  1. 张总 - 总经理办公室 - 总经理");
        System.out.println("  2. 李经理 - 销售部 - 部门主管 (上级：张总)");
        System.out.println("  3. 小王 - 销售部 - 普通员工 (上级：李经理)");
        System.out.println("  4. 小张 - 销售部 - 普通员工 (上级：李经理)");
        System.out.println("  5. 王财务 - 财务部 - 财务 (上级：张总)");
        System.out.println("========================================");
    }
}
