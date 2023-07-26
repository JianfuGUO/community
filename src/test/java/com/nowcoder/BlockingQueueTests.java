package com.nowcoder;

import org.springframework.ui.context.Theme;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 阻塞队列
 * @Author Xiao Guo
 * @Date 2023/4/21
 */

public class BlockingQueueTests {

    public static void main(String[] args) {

        // 阻塞队列
        // 队列容量为10
        BlockingQueue queue = new ArrayBlockingQueue(10);

        // 生产者线程不断产生数据
        new Thread(new Producer(queue)).start();

        // 消费者线程不断消费数据
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();

    }
}

// 生产者（线程）
class Producer implements Runnable{

    // 线程交给阻塞队列处理
    private BlockingQueue<Integer> queue;

    // 有参构造器
    // 创建对象时就将阻塞队列传进来
    public Producer(BlockingQueue<Integer> queue){
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            // 产生数据
            for (int i = 0; i < 100; i++) {
                Thread.sleep(20);
                // 使用阻塞队列来管理数据
                queue.put(i);
                // 打印当前线程名
                System.out.println(Thread.currentThread().getName() + "生产：" + queue.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

// 消费者（线程）
class Consumer implements Runnable{

    // 线程交给阻塞队列处理
    private BlockingQueue<Integer> queue;

    // 有参构造器
    // 创建对象时就将阻塞队列传进来
    public Consumer(BlockingQueue<Integer> queue){
        this.queue = queue;
    }

    @Override
    public void run() {
        // 设计成只要有数据就一直消费
        try {
            while (true){
                // 消费者的消费能力没有生产者快
                Thread.sleep(new Random().nextInt(1000));
                // 使用数据
                queue.take();
                // 打印当前线程名
                System.out.println(Thread.currentThread().getName() + "消费：" + queue.size());

            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}