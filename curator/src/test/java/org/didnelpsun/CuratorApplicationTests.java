// CuratorApplicationTests.java
package org.didnelpsun;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.nio.charset.StandardCharsets;

@SpringBootTest
class CuratorApplicationTests {
    private CuratorFramework curatorFramework;
    @Autowired
    public void setCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }
    @Test
    // 创建新节点
    public void createNode() throws Exception{
        // 添加持久节点，默认为持久节点
        String path = curatorFramework.create().forPath("/test");
        // 设置数据
        curatorFramework.setData().forPath("/test", "test".getBytes(StandardCharsets.UTF_8));
        // 添加持久序号节点
        // withMode表示这个节点的类型
        String path2 = curatorFramework.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath("/test/ps", "ps".getBytes(StandardCharsets.UTF_8));
        // 添加临时序号节点
        String path3 = curatorFramework.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/test/es", "es".getBytes(StandardCharsets.UTF_8));
        System.out.printf("Curator创建持久节点%s成功",path);
        System.out.printf("Curator创建持久序号节点%s成功",path2);
        System.out.printf("Curator创建临时序号节点%s成功",path3);
        // 获取某个节点的所有子节点
        System.out.println(curatorFramework.getChildren().forPath("/"));
    }
    @Test
    // 获取节点数据
    public void getData() throws Exception{
        byte[] bytes = curatorFramework.getData().forPath("/test/ps");
        System.out.println(new String(bytes));
    }
    @Test
    // 创建父结点
    public void createWithParent() throws Exception{
        // 检查某节点是否存在
        // 返回值为Stat类型，如果不存在就返回null
        if(curatorFramework.checkExists().forPath("/test")!=null){
            String path = curatorFramework.create().creatingParentsIfNeeded().forPath("/test/parent/child");
            System.out.printf("Curator创建节%s成功", path);
        }
    }
    @Test
    // 获取读锁
    public void getReadLock() throws Exception{
        // 读写锁，指明创建读写锁的路径
        InterProcessReadWriteLock interProcessReadWriteLock = new InterProcessReadWriteLock(curatorFramework, "/lock");
        // 获取读锁对象
        InterProcessLock interProcessLock = interProcessReadWriteLock.readLock();
        System.out.println("等待获取读锁");
        // 获取读锁
        // 是尝试获取锁，如果没有拿到就会一直阻塞在这里
        interProcessLock.acquire();
        for(int i=0;i<10;i++){
            Thread.sleep(3000);
            System.out.println(i);
        }
        // 释放锁
        interProcessLock.release();
        System.out.println("等待释放锁");
    }
    @Test
    // 获取写锁
    public void getWriteLock() throws Exception{
        // 读写锁，指明创建读写锁的路径
        InterProcessReadWriteLock interProcessReadWriteLock = new InterProcessReadWriteLock(curatorFramework, "/lock");
        // 获取写锁对象
        InterProcessLock interProcessLock = interProcessReadWriteLock.writeLock();
        System.out.println("等待获取写锁");
        // 获取写锁
        interProcessLock.acquire();
        for(int i=0;i<10;i++){
            Thread.sleep(3000);
            System.out.println(i);
        }
        // 释放锁
        interProcessLock.release();
        System.out.println("等待释放锁");
    }
    @Test
    // 删除节点
    public void deleteNode() throws Exception{
        // guaranteed表示如果服务端可能删除成功，但是client没有接收到删除成功的提示，Curator将会在后台持续尝试删除该节点
        // deletingChildrenIfNeeded表示如果存在子节点就一起删除
        curatorFramework.delete().guaranteed().deletingChildrenIfNeeded().forPath("/test/parent");
    }
    @Test
    // 监听节点
    public void addNodeListener() throws Exception{
        // 需要导入curator-recipes依赖
        // NodeCache使用节点数据作为本地缓存使用。这个类可以对节点进行监听，能够处理节点的增删改事件，数据同步等。 还可以通过注册自定义监听器来更细节的控制这些数据变动操作。
        NodeCache nodeCache = new NodeCache(curatorFramework,"/test");
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                System.out.println("/test路径发生变化");
                System.out.printf("/test数据为%s", new String(curatorFramework.getData().forPath("/test")));
            }
        });
        nodeCache.start();
        // 阻塞方法，让方法一直调用
        System.in.read();
    }
}
