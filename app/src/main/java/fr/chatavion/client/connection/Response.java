package fr.chatavion.client.connection;

import java.util.List;
import java.util.Objects;

public class Response {

    int statut;
    List<String> results;

    public Response(int result, List<String> results) {
        this.statut = result;
        this.results = results;
    }

    public int statut() {
        return statut;
    }

    public List<String> results() {
        return results;
    }
}
