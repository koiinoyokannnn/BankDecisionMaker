package com.bank;

import com.bank.random.ExponentialDistribution;
import com.bank.random.LCG;
import lombok.Data;

import java.util.*;

/**Класс обработки модельных событий*/
@Data
public class Simulation {
    /**Список событий*/
    private PriorityQueue<Event> eventQueue = new PriorityQueue<>();
    private ArrayList<Event> eventQueueForPrint = new ArrayList<>();
    /**Массив с 8-ю операторами*/
    private Operator[] operators = new Operator[8];
    /**Разделенные очереди (Массив с обычными очередями. Каждая очередь принимает объекты <i>Client</i>*/
    private Queue<Client>[] separateQueues;
    /**Единая очередь. Принимает объекты <i>Client</i>*/
    private Queue<Client> singleQueue;
    /**ЛКМ принимающий в качестве seed значение из статического методо <i>seed</i> в <i>Configuration</i>*/
    private LCG lcg;
    Configuration configuration;

    /**Текущее время*/
    private double currentTime = 0.0;
    /**Счетчик <i>id</i> клиента*/
    private int clientIdCounter = 1;

    //статистика

    /**Массив списков с временами ожидания в зависимости от типа операции*/
    private List<Double>[] waitTimesByType = new List[5];
    /**Массив со значениями занятости каждого оператора*/
    private double[] operatorBusyTime = new double[8];
    private int simulationTimeSeconds;

    /**Конструктор*/
    public Simulation(Configuration configuration) {
        this.configuration = configuration;
        this.lcg = new LCG(configuration.getSeed());
        for (int i = 0; i < operators.length; i++) {
            operators[i] = new Operator();
        }

        if (configuration.isSingleQueue()) {
            singleQueue = new LinkedList<>();
        } else {
            separateQueues = new Queue[8];
            for (int i = 0; i < separateQueues.length; i++) {
                separateQueues[i] = new LinkedList<>();
            }
        }

        for (int i = 0; i < 5; i++) {
            waitTimesByType[i] = new ArrayList<>();
        }
        this.simulationTimeSeconds = configuration.getSimulationTimeSeconds();

        // Генерируем ПЕРВОГО клиента заранее и привязываем к событию
        double lambdaPerSec = configuration.getLambda() / 3600.0;
        double firstArrival = ExponentialDistribution.get(lambdaPerSec, lcg);

        if (firstArrival < simulationTimeSeconds) { // Проверка границы!
            int opType = selectOperationType();
            int serviceTime = configuration.getDurations()[opType - 1];
            Client firstClient = new Client(
                clientIdCounter++,
                (int) firstArrival,
                serviceTime,
                opType
            );
            Event firstEvent = new Event(EventType.ARRIVAL, firstArrival, firstClient, null);
            eventQueue.add(firstEvent);
            eventQueueForPrint.add(firstEvent);
        }
    }

    /**Метод псевдослучайно определяющий тип операции в зависимости от вероятностей
     *<i>probabilities</i> из класса <i>Configuration</i>*/
    private int selectOperationType() {
        double r = lcg.getRand();
        double cumulative = 0.0;
        for (int i = 0; i < configuration.getProbabilities().length; i++) {
            cumulative += configuration.getProbabilities()[i];
            if (r < cumulative) return i + 1; // типы 1-5
        }
        return 5;
    }
    /**Запуск основной симуляции. Пока список всех событий не пуст, происходит "отлов" и обработка каждого события*/
    public void run() {
        while (!eventQueue.isEmpty()) {
            Event event = eventQueue.poll();
            if (event == null) break;
            currentTime = event.getTime();

            switch (event.getType()) {
                case ARRIVAL:
                    handleArrival(event); // Передаём событие целиком
                    break;
                case COMPLETING:
                    handleCompleting(event.getOperator());
                    break;
            }
        }
    }
    /**Обработка события <i>Arrival</i>. */
    private void handleArrival(Event event) {
        Client client = event.getClient();

        Operator freeOp = findFreeOperator();
        if (freeOp != null) {
            startService(client, freeOp);
        } else {
            if (configuration.isSingleQueue()) {
                singleQueue.add(client);
            } else {
                int shortestQueue = findShortestQueue();
                separateQueues[shortestQueue].add(client);
            }
        }

        double lambdaPerSec = configuration.getLambda() / 3600.0;
        double nextInterarrival = ExponentialDistribution.get(lambdaPerSec, lcg);
        double nextArrival = currentTime + nextInterarrival;
        if (nextArrival < simulationTimeSeconds) { // Проверяем целевое время
            // создаём клиента ЗАРАНЕЕ для события
            int opType = selectOperationType();
            Client nextClient = new Client(
                clientIdCounter++,
                (int) nextArrival,
                configuration.getDurations()[opType - 1],
                opType
            );
            Event arrivalEvent = new Event(EventType.ARRIVAL, nextArrival, nextClient, null);
            eventQueue.add(arrivalEvent);
            eventQueueForPrint.add(arrivalEvent);
        }
    }

    /**Обработка события <i>Completing</i>. */
    private void handleCompleting(Operator op) {
        op.setBusy(false);

        operatorBusyTime[getOperatorIndex(op)] += (currentTime - op.getServiceStartTime());

        Client nextClient = null;

        if (configuration.isSingleQueue()) {
            nextClient = singleQueue.poll();
        } else {
            int idx = getOperatorIndex(op);
            if (!separateQueues[idx].isEmpty()) {
                nextClient = separateQueues[idx].poll();
            }
        }

        if (nextClient != null) {
            startService(nextClient, op);
        }
    }

    /**Метод для нахождения свободного оператора в списке операторов*/
    private Operator findFreeOperator() {
        for (Operator op : operators) {
            if (!op.isBusy()) {
                return op;
            }
        }
        return null;
    }

    /**Начало обработки заявки клиента. Назначаем текущего оператора занятым, выставляем начало времени обработки.
     * Высчитываем время ожидания, так же добавляем его в общий список времен ожидания по типам для будущего анализа статистики. Также мы дублируем основную очередь
     * событий в обычный ArrayList<>(), для вывода при необходимости в консоль */
    private void startService(Client client, Operator op) {
        op.setBusy(true);
        op.setServiceStartTime(currentTime);

        double waitTime = currentTime - client.getArrivalTime();
        client.setWaitTime(waitTime);

        waitTimesByType[client.getOperationType() - 1].add(waitTime);

        double endTime = currentTime + client.getServiceTime();
        client.setFullSpentTime(currentTime - client.getArrivalTime());
        Event completingEvent = new Event(EventType.COMPLETING, endTime, client, op);
        eventQueue.add(completingEvent);
        eventQueueForPrint.add(completingEvent);
    }

    /**Метод для нахождения более короткой очереди в списке раздельных очередей*/
    private int findShortestQueue() {
        int shortest = 0;
        int minLength = separateQueues[0].size();
        for (int i = 1; i < 8; i++) {
            if (separateQueues[i].size() < minLength) {
                minLength = separateQueues[i].size();
                shortest = i;
            }
        }
        return shortest;
    }

    /**Метод возвращающий индекс оператора*/
    private int getOperatorIndex(Operator op) {
        for (int i = 0; i < operators.length; i++) {
            if (operators[i] == op) return i;
        }
        return -1;
    }

    /**Метод для вывода всех событий и некоторых статистик в консоль (Использовалось для проверки промежуточных изменений)*/
    public void printEvents() {
        int i = 1;
        eventQueueForPrint.sort(Comparator.comparing(Event::getTime));
        for (var event : eventQueueForPrint) {
            String eventType = event.getType().toString();
            String clientId = new String();
            if (event.getClient() == null) {
                clientId = "null";
            }
            else clientId = Integer.toString(event.getClient().getId());
            String operatorId = new String();
            if (event.getOperator() == null) {
                operatorId = "null";
            }
            else operatorId = Integer.toString(getOperatorIndex(event.getOperator()));
            String time = Double.toString(event.getTime());


            System.out.print(Integer.toString(i++) + ": " + eventType + ", " + clientId + ", " + operatorId + ", " + Integer.toString(event.getClient().getOperationType()) + ", " + time);
            if (event.getType() == EventType.COMPLETING) System.out.print(", " + Double.toString(event.getClient().getWaitTime()) + "\n");
            else System.out.print("\n");
        }
        System.out.println(getAvgWaitTime());
        System.out.println(getAvgOperatorBusyTime());
        System.out.println(getAvgTimeSpent());
    }

    /**Метод, возвращающий среднее время ожидания в модели*/
    public double getAvgWaitTime() {
        ArrayList<Double> avgTimes = new ArrayList();
        for (var type : waitTimesByType) {
            avgTimes.add(type.stream()
                .mapToDouble(Double::doubleValue) // Преобразуем в DoubleStream
                .average() // Находим среднее
                .orElse(0.0));
        }
        return (avgTimes.stream()
            .mapToDouble(Double::doubleValue) // Преобразуем в DoubleStream
            .average() // Находим среднее
            .orElse(0.0));
    }
    /**Метод, возвращающий среднее время занятости операторов в модели*/
    public double getAvgOperatorBusyTime() {
        return Arrays.stream(operatorBusyTime)
            .average() // Возвращает OptionalDouble
            .orElse(0.0); // Если массив пуст
    }

    /**Метод, возвращающий среднее время общего нахождения клиента в модели*/
    public double getAvgTimeSpent() {
        ArrayList<Double> timesSpent = new ArrayList<>();
        for (var e : eventQueueForPrint) {
            if (e.getType() == EventType.COMPLETING) {
                timesSpent.add(e.getClient().getFullSpentTime());
            }
        }
        return (timesSpent.stream()
            .mapToDouble(Double::doubleValue) // Преобразуем в DoubleStream
            .average() // Находим среднее
            .orElse(0.0));
    }

    /**Метод, возвращающий среднее время (массив) ожидания относительно каждого типа операции в модели*/
    public double[] getAvgWaitTimeByType() {
        double[] avgs = new double[waitTimesByType.length];
        for (int i = 0; i < waitTimesByType.length; i++) {
            avgs[i] = waitTimesByType[i].stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        }
        return avgs;
    }

    /**Метод, возвращающий средний процент (массив) занятости всех операторов в модели*/
    public double[] getOperatorUtilization() {
        double[] utilization = new double[operatorBusyTime.length];
        for (int i = 0; i < operatorBusyTime.length; i++) {
            utilization[i] = (operatorBusyTime[i] / simulationTimeSeconds) * 100.0;
        }
        return utilization;
    }

}
