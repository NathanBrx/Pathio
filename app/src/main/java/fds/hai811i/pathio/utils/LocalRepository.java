package fds.hai811i.pathio.utils;

import java.util.ArrayList;
import java.util.List;
import fds.hai811i.pathio.model.Post;
import fds.hai811i.pathio.model.User;

/**
 * A simple Singleton to manage posts in memory during the session.
 * This allows the app to be fully functional (Add -> View) without a backend.
 */
public class LocalRepository {
    private static LocalRepository instance;
    private final List<Post> posts = new ArrayList<>();
    private final User currentUser = new User(1, "Moi", null);

    private LocalRepository() {
        // Initial mock posts
        posts.add(new Post(101, new User(2, "Thomas_Travel", null), "Place de la Comédie, Montpellier", "uploads/mountain.jpg", "Un après-midi ensoleillé sur la place !", "Il y a 2h", 24));
        posts.add(new Post(102, new User(3, "Alice_Explorer", null), "Musée Fabre", null, "Les expositions sont magnifiques cette saison.", "Il y a 5h", 12));
    }

    public static synchronized LocalRepository getInstance() {
        if (instance == null) instance = new LocalRepository();
        return instance;
    }

    public List<Post> getPosts() {
        return new ArrayList<>(posts); // Return a copy
    }

    public void addPost(String caption, String location, String imageUri) {
        // id 1000+ for local posts
        int id = 1000 + posts.size();
        posts.add(0, new Post(id, currentUser, location, imageUri, caption, "À l'instant", 0));
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
