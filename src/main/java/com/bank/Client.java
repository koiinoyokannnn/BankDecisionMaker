package com.bank;

import com.bank.random.LCG;
import com.bank.random.PoissonDistribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Класс клиента с полями <b>id</b>, <b>serviceTime</b> и <b>arrivalTime</b>
*/
public class Client {
    /*Поле id*/
    private int id;
    /*Поле времени прибытия*/
    private int serviceTime;
    private int arrivalTime;

    public Client(int id, int serviceTime, int arrivalTime) {
        this.id = id;
        this.serviceTime = serviceTime;
        this.arrivalTime = arrivalTime;
    }

    public static Client[] generateClients(int lambda) {
        LCG lcg = new LCG(42);
        List<Integer> arrivals = new ArrayList<Integer>();

        for (int i = 0; i < PoissonDistribution.get(lambda, lcg); i++) {
            arrivals.add((int) (lcg.getRand() * 3600));
        }
        Client[] clients = new Client[arrivals.size()];
        Collections.sort(arrivals);
        for (int i = 0; i < arrivals.size(); i++) {
            var probability = lcg.getRand();
            int serviceTime;
            if (probability < 0.1) serviceTime = 45;
            else if (probability < 0.29) serviceTime = 75;
            else if (probability < 0.61) serviceTime = 100;
            else if (probability < 0.85) serviceTime = 150;
            else serviceTime = 300;
            clients[i] = new Client(i + 1, serviceTime, arrivals.get(i));
        }
        return clients;
    }
}
