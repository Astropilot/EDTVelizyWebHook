package launcher;

import java.util.HashMap;
import java.util.Scanner;

import utils.Data;
import utils.DiscordMessageWriter;
import workers.DetectorWorker;

/**
 * Classe principale du programme contenant le main
 * 
 * @author Yohann MARTIN
 *
 */
public class Main {
	
	/**
	 * La liste des workers au travail
	 */
	static HashMap<DetectorWorker, Thread> workersList;

	/**
	 * Fonction de démarrage du programme appelée automatiquement
	 * 
	 * @param args Les arguments de démarrage du programme (optionnel)
	 */
	public static void main(String[] args) {
		
		// On initialise nos différents workers, puis on les lance
		
		initAndStartWorkers();
		
		// On attend la commande "exit" pour quitter le programme
		
		while(true) {
        	Scanner sc = new Scanner(System.in);
        	String command = sc.nextLine();
        	
        	if(command.equals("exit")) {
        		System.out.println("Fermeture des Workers...");
        		sc.close();
        		stopAllThreads();
        		System.out.println("All done ! Good bye~");
        		System.exit(0);
        	}

        }
	}
	
	/**
	 * Fonction qui initialise et démarrage les Workers
	 */
	private static void initAndStartWorkers() {
		workersList = new HashMap<DetectorWorker, Thread>();
		DetectorWorker worker1 = new DetectorWorker(Data.NAME_WORKER1, Data.ID_INFO1_A, Data.WEBHOOK_INFO1_A);
		DetectorWorker worker2 = new DetectorWorker(Data.NAME_WORKER2, Data.ID_INFO1_B, Data.WEBHOOK_INFO1_B);
		DetectorWorker worker3 = new DetectorWorker(Data.NAME_WORKER3, Data.ID_INFO1_C, Data.WEBHOOK_INFO1_C);
		DetectorWorker worker4 = new DetectorWorker(Data.NAME_WORKER4, Data.ID_INFO2_A, Data.WEBHOOK_INFO2_A);
		DetectorWorker worker5 = new DetectorWorker(Data.NAME_WORKER5, Data.ID_INFO2_B, Data.WEBHOOK_INFO2_B);
		DetectorWorker worker6 = new DetectorWorker(Data.NAME_WORKER6, Data.ID_INFO2_FA, Data.WEBHOOK_INFO2_FA);
		
		workersList.put(worker1, new Thread(worker1));
		workersList.put(worker2, new Thread(worker2));
		workersList.put(worker3, new Thread(worker3));
		workersList.put(worker4, new Thread(worker4));
		workersList.put(worker5, new Thread(worker5));
		workersList.put(worker6, new Thread(worker6));
		
		for(DetectorWorker worker : workersList.keySet()) {
			System.out.println(String.format("Démarrage du Worker %s", worker.getWorkerName()));
			workersList.get(worker).start();
		}
	}
	
	/**
	 * Fonction qui va arrêter tout les Workers
	 */
	private static void stopAllThreads() {
		for(DetectorWorker worker : workersList.keySet()) {
			worker.terminate();
			try {
				workersList.get(worker).join();
			} catch (InterruptedException e) {
				System.err.println("Impossible de fermer un Worker (Thread): " + e.getMessage());
			}
		}
	}

}
