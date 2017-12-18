package me.dags.massblockr;

import me.dags.massblockr.client.Client;

/**
 * @author dags <dags@dags.me>
 */
public interface App {

    void launch(String[] args);

    void onError(Throwable t);

    Client newClient(ConverterOptions options);

    default void submit(ConverterOptions options) {
        System.out.printf("Converting with options: %s\n", options);
        Client client = newClient(options);
        new Thread(() -> Converter.run(this, client, options)).start();
    }
}
