package com.packetinspector.dpi.service;

import com.packetinspector.dpi.model.PacketJob;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.LongAdder;

final class LoadBalancer implements Runnable {
    record Stats(long packetsReceived, long packetsDispatched) {}

    private final BlockingQueue<PacketJob> input;
    private final List<BlockingQueue<PacketJob>> fastPathQueues;
    private final LongAdder packetsReceived = new LongAdder();
    private final LongAdder packetsDispatched = new LongAdder();
    private final Thread thread;
    private volatile IOException failure;

    LoadBalancer(int id, int queueSize, List<BlockingQueue<PacketJob>> fastPathQueues) {
        input = new ArrayBlockingQueue<>(queueSize);
        this.fastPathQueues = List.copyOf(fastPathQueues);
        thread = new Thread(this, "dpi-lb-" + id);
    }

    BlockingQueue<PacketJob> input() {
        return input;
    }

    void start() {
        thread.start();
    }

    void finish() throws InterruptedException {
        input.put(PacketJob.stopSignal());
    }

    void join() throws InterruptedException {
        thread.join();
    }

    Stats stats() {
        return new Stats(packetsReceived.sum(), packetsDispatched.sum());
    }

    IOException failure() {
        return failure;
    }

    @Override
    public void run() {
        try {
            PacketJob job;
            while (!(job = input.take()).endOfStream()) {
                packetsReceived.increment();
                int index = Math.floorMod(job.tuple().hashCode(), fastPathQueues.size());
                fastPathQueues.get(index).put(job);
                packetsDispatched.increment();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            failure = new IOException("Load balancer was interrupted", exception);
        }
    }
}
