import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SocialApp {

    // ===== In-memory data models =====
    static class User implements Serializable {
        private static final long serialVersionUID = 1L;
        String firstName, lastName, gender, email, dob, password;
        java.util.List<Post> posts = new ArrayList<>();

        User(String f, String l, String g, String e, String d, String p) {
            firstName = f;
            lastName = l;
            gender = g;
            email = e;
            dob = d;
            password = p;
        }
    }

    static class Post implements Serializable {
        private static final long serialVersionUID = 1L;
        String text;
        String timestamp;
        int likes = 0;

        Post(String text) {
            this.text = text;
            this.timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("MMM d, yyyy • h:mm a"));
        }
    }

    static Map<String, User> users = new HashMap<>(); // key = email
    static User currentUser = null;
    static boolean darkMode = false;
    static final String SETTINGS_FILE = "loop_settings.ser";
    static final String LAST_USER_FILE = "loop_last_user.ser";

    // ===== Main frame =====
    JFrame frame = new JFrame("Loop • Social Media");
    CardLayout cardLayout = new CardLayout();
    JPanel mainPanel = new JPanel(cardLayout);

    public SocialApp() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        loadSettings();
        applyTheme();
        frame.setLocationRelativeTo(null);

        mainPanel.add(loginPanel(), "login");
        mainPanel.add(registerPanel(), "register");
        mainPanel.add(homePanel(), "home");
        mainPanel.add(profilePanel(), "profile");

        frame.add(mainPanel);
        loadData();
        loadLastUser();
        frame.setVisible(true);
    }

    // ===== Login Panel =====
    JPanel loginPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 1));
        JLabel welcomeLabel = new JLabel("Welcome to Loop", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(welcomeLabel);
        panel.setBackground(new Color(30, 34, 90));
        welcomeLabel.setForeground(Color.WHITE);
        JTextField emailField = new JTextField();
        JPasswordField passField = new JPasswordField();

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");

        JLabel emailLabel = new JLabel("Email:");
        JLabel passwordLabel = new JLabel("Password:");

        emailLabel.setForeground(Color.WHITE);
        passwordLabel.setForeground(Color.WHITE);

        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passField);
        panel.add(loginBtn);
        panel.add(registerBtn);

        loginBtn.addActionListener(e -> {
            String email = emailField.getText();
            String pass = new String(passField.getPassword());

            if (users.containsKey(email) && users.get(email).password.equals(pass)) {
                currentUser = users.get(email);
                saveLastUser();
                refreshFeed();
                cardLayout.show(mainPanel, "home");
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid login credentials");
            }
        });

        registerBtn.addActionListener(e -> cardLayout.show(mainPanel, "register"));

        return panel;
    }

    // ===== Register Panel =====
    JPanel registerPanel() {
        JPanel panel = new JPanel(new GridLayout(9, 2));

        JTextField fName = new JTextField();
        JTextField lName = new JTextField();
        JTextField gender = new JTextField();
        JTextField email = new JTextField();
        JTextField dob = new JTextField();
        JPasswordField pass = new JPasswordField();

        JButton createBtn = new JButton("Create Account");
        JButton backBtn = new JButton("Back");

        panel.add(new JLabel("First Name:")); panel.add(fName);
        panel.add(new JLabel("Last Name:")); panel.add(lName);
        panel.add(new JLabel("Gender:")); panel.add(gender);
        panel.add(new JLabel("Email:")); panel.add(email);
        panel.add(new JLabel("Date of Birth:")); panel.add(dob);
        panel.add(new JLabel("Password:")); panel.add(pass);
        panel.add(createBtn);
        panel.add(backBtn);

        createBtn.addActionListener(e -> {
            if (users.containsKey(email.getText())) {
                JOptionPane.showMessageDialog(frame, "Email already exists");
                return;
            }

            User u = new User(
                    fName.getText(),
                    lName.getText(),
                    gender.getText(),
                    email.getText(),
                    dob.getText(),
                    new String(pass.getPassword())
            );

            users.put(u.email, u);
            saveData();
            JOptionPane.showMessageDialog(frame, "Account created successfully!");
            cardLayout.show(mainPanel, "login");
        });

        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        return panel;
    }

    // ===== Home Panel =====
    JPanel feedPanel = new JPanel();

    JPanel homePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBackground(new Color(245, 247, 255));

        JTextField postField = new JTextField();
        JButton postBtn = new JButton("Post");
        JButton profileBtn = new JButton("Profile");

        postBtn.addActionListener(e -> {
            if (!postField.getText().isEmpty()) {
                Post post = new Post(postField.getText());
                currentUser.posts.add(post);
                saveData();
                refreshFeed();
                postField.setText("");
            }
        });

        profileBtn.addActionListener(e -> cardLayout.show(mainPanel, "profile"));

        JPanel top = new JPanel(new BorderLayout());
        top.add(postField, BorderLayout.CENTER);
        top.add(postBtn, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(feedPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(profileBtn, BorderLayout.SOUTH);

        return panel;
    }


    private JPanel createPostBubble(String author, Post post) {
        JPanel bubble = new JPanel(new BorderLayout());
        bubble.setBackground(Color.WHITE);
        bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel nameLabel = new JLabel(author);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel timeLabel = new JLabel(post.timestamp);
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(nameLabel, BorderLayout.WEST);
        header.add(timeLabel, BorderLayout.EAST);

        JTextArea postText = new JTextArea(post.text);
        postText.setLineWrap(true);
        postText.setWrapStyleWord(true);
        postText.setEditable(false);
        postText.setOpaque(false);
        postText.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JButton likeBtn = new JButton("❤ " + post.likes);
        likeBtn.setFocusPainted(false);
        likeBtn.addActionListener(e -> {
            post.likes++;
            likeBtn.setText("❤ " + post.likes);
            saveData();
        });

        bubble.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(postText, BorderLayout.CENTER);
        content.add(likeBtn, BorderLayout.SOUTH);

        bubble.add(content, BorderLayout.CENTER);

        bubble.setMaximumSize(new Dimension(Integer.MAX_VALUE, bubble.getPreferredSize().height));
        bubble.setAlignmentX(Component.LEFT_ALIGNMENT);

        return bubble;
    }
    // ===== Helper method to refresh feed =====
    private void refreshFeed() {
        feedPanel.removeAll();

        java.util.List<Map.Entry<User, Post>> allPosts = new ArrayList<>();
        for (User u : users.values()) {
            for (Post p : u.posts) {
                allPosts.add(new AbstractMap.SimpleEntry<>(u, p));
            }
        }

        allPosts.sort((a, b) -> b.getValue().timestamp.compareTo(a.getValue().timestamp));

        for (Map.Entry<User, Post> entry : allPosts) {
            JPanel bubble = createPostBubble(entry.getKey().firstName, entry.getValue());
            feedPanel.add(bubble);
            feedPanel.add(Box.createVerticalStrut(10));
        }

        feedPanel.revalidate();
        feedPanel.repaint();
    }

    // ===== Profile Panel =====
    JPanel profilePanel() {
        JPanel panel = new JPanel(new GridLayout(7, 1));

        JLabel nameLabel = new JLabel();
        JToggleButton darkModeToggle = new JToggleButton("Dark Mode");
        darkModeToggle.setFocusPainted(false);
        darkModeToggle.setBackground(Color.LIGHT_GRAY);
        darkModeToggle.setOpaque(true);
        JButton signOutBtn = new JButton("Sign Out");
        JButton backBtn = new JButton("Back to Home");

        panel.add(nameLabel);
        panel.add(darkModeToggle);
        panel.add(new JLabel("You can extend this page to edit profile info."));
        panel.add(signOutBtn);
        panel.add(backBtn);

        panel.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                nameLabel.setText("Logged in as: " + currentUser.firstName + " " + currentUser.lastName);
                darkModeToggle.setSelected(darkMode);
            }
        });

        darkModeToggle.addActionListener(e -> {
            darkMode = darkModeToggle.isSelected();
            applyTheme();
            saveSettings();
        });

        signOutBtn.addActionListener(e -> {
            saveData();
            currentUser = null;
            saveLastUser();
            cardLayout.show(mainPanel, "login");
        });

        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "home"));

        return panel;
    }

    private void saveSettings() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SETTINGS_FILE))) {
            out.writeBoolean(darkMode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSettings() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SETTINGS_FILE))) {
            darkMode = in.readBoolean();
        } catch (IOException e) {
            darkMode = false;
        }
    }

    private void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("loop_data.ser"))) {
            out.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("loop_data.ser"))) {
            Object obj = in.readObject();
            if (obj instanceof Map) {
                users = (Map<String, User>) obj;
            } else {
                users = new HashMap<>();
            }
        } catch (Exception e) {
            users = new HashMap<>();
        }
    }

    private void saveLastUser() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(LAST_USER_FILE))) {
            out.writeObject(currentUser != null ? currentUser.email : null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLastUser() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(LAST_USER_FILE))) {
            Object obj = in.readObject();
            if (obj instanceof String && users.containsKey(obj)) {
                currentUser = users.get(obj);
                refreshFeed();
                cardLayout.show(mainPanel, "home");
            }
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SocialApp::new);
    }

    private void applyTheme() {
        Color bg = darkMode ? new Color(18, 18, 18) : new Color(245, 247, 255);
        Color panelBg = darkMode ? new Color(30, 30, 30) : Color.WHITE;
        Color text = darkMode ? Color.WHITE : Color.BLACK;

        frame.getContentPane().setBackground(bg);
        feedPanel.setBackground(bg);

        SwingUtilities.updateComponentTreeUI(frame);
    }
}