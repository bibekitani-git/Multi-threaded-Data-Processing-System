// --- Java Implementation ---

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

class Task {
    int id;
    public Task(int id) {
        this.id = id;
    }
}

class TaskQueue {
    private final Queue<Task> queue = new LinkedList<>();

    public synchronized void addTask(Task task) {
        queue.add(task);
        notifyAll();
    }

    public synchronized Task getTask() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return queue.poll();
    }
}

class Worker implements Runnable {
    private final TaskQueue taskQueue;
    private final List<String> resultList;
    private final int workerId;

    public Worker(TaskQueue taskQueue, List<String> resultList, int workerId) {
        this.taskQueue = taskQueue;
        this.resultList = resultList;
        this.workerId = workerId;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Task task = taskQueue.getTask();
                if (task == null) break;
                System.out.println("Worker " + workerId + " processing task " + task.id);
                Thread.sleep(500); // simulate processing
                synchronized (resultList) {
                    resultList.add("Worker " + workerId + " processed task " + task.id);
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Worker " + workerId + " interrupted.");
        }
    }
}

public class DataProcessingSystem {
    public static void main(String[] args) throws IOException, InterruptedException {
        TaskQueue queue = new TaskQueue();
        List<String> resultList = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (int i = 1; i <= 10; i++) {
            queue.addTask(new Task(i));
        }

        for (int i = 0; i < 4; i++) {
            executor.submit(new Worker(queue, resultList, i));
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output_java.txt"))) {
            for (String result : resultList) {
                writer.write(result);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("File writing error: " + e.getMessage());
        }
    }
}
