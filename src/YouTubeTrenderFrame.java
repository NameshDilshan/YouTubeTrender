import model.YouTubeVideo;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.io.FileNotFoundException;
import java.awt.event.*;
import java.util.List;

public class YouTubeTrenderFrame extends JFrame {

    private JList<String> videoList;
    private JLabel thumbnailLabel, titleLabel;
    private JTextArea descriptionLabel;  // Changed to JTextArea for better scroll behavior
    private JTable infoTable;
    private JPanel chartPanelLikes, chartPanelComments, chartPanelViews;
    private JPanel otherPanel;
    private List<YouTubeVideo> videoData;
    private JRadioButton channelRadioButton, dateRadioButton, viewRadioButton, descriptionRadioButton;
    String sortingCriteria = "Channel";
    String jsonFileLocation = "data/youtubedata_15_50.json";
    private Map<String, Integer> wordCountMap = new HashMap<>();
    private Map<String, List<YouTubeVideo>> wordVideoMap = new HashMap<>();


    public YouTubeTrenderFrame() {
        setTitle("YouTube Trender");
        setSize(1200, 1000);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize videoData list
        videoData = new ArrayList<>();

        // Main Panels Layout
        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();

        // Combine leftPanel and rightPanel into a parent panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(leftPanel, BorderLayout.WEST);
        contentPanel.add(rightPanel, BorderLayout.CENTER);

        // Wrap the content panel in a JScrollPane to enable scrolling
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Add the JScrollPane to the frame
        add(scrollPane, BorderLayout.CENTER);

        // Center the window on the screen
        setLocationRelativeTo(null);

        // Load JSON Data
        loadData();
    }

    // Load JSON data and populate the list
    private void loadData() {
        YouTubeDataParser parser = new YouTubeDataParser();
        try {
            System.out.println(jsonFileLocation);
            videoData = parser.parse(jsonFileLocation);
            System.out.println(videoData.size());
            switch(sortingCriteria) {
                case "Date":
                    videoData.sort((v1, v2) -> v1.getDate().compareTo(v2.getDate()));
                    break;
                case "View":
                    videoData.sort((v1, v2) -> Integer.compare(v2.getViewCount(), v1.getViewCount())); // Descending order
                    break;
                case "Description":
                    videoData.sort((v1, v2) -> Integer.compare(v1.getDescription().length(), v2.getDescription().length()));
                    break;
                default:
                    videoData.sort((v1, v2) -> v1.getChannel().compareToIgnoreCase(v2.getChannel()));
                    break;
            }
            DefaultListModel<String> videoTitles = new DefaultListModel<>();
            for (YouTubeVideo video : videoData) {
                videoTitles.addElement(video.getTitle());
            }
            videoList.setModel(videoTitles);
        } catch (FileNotFoundException | YouTubeDataParserException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

//    // Create the left panel for the video list
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(300, 800));

        videoList = new JList<>();
        videoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        videoList.addListSelectionListener(e -> updateVideoDetails());

        JScrollPane videoScrollPane = new JScrollPane(videoList);
        videoScrollPane.setPreferredSize(new Dimension(300, 500));
        leftPanel.add(videoScrollPane, BorderLayout.NORTH);

        JPanel trendingDetailsPanel = createTrendingDetailsPanel();
        leftPanel.add(trendingDetailsPanel, BorderLayout.CENTER);
        return leftPanel;
    }

    private JPanel createTrendingDetailsPanel() {
        JPanel trendingPanel = new JPanel();
        trendingPanel.setLayout(new BorderLayout());  // Using BorderLayout for simpler alignment
        trendingPanel.setBorder(BorderFactory.createTitledBorder("Trending Topics"));

        // Button to trigger the indexing
        JButton indexButton = new JButton("Index Trending Topics");

        // JList to display trending words along with their counts
        DefaultListModel<String> wordListModel = new DefaultListModel<>();
        JList<String> trendingList = new JList<>(wordListModel);
        JScrollPane trendingScrollPane = new JScrollPane(trendingList);
        trendingScrollPane.setPreferredSize(new Dimension(300, 100));  // Set smaller preferred size for the south panel

        // Text area to display details for the selected word
        JTextArea wordDetails = new JTextArea(5, 30);
        wordDetails.setLineWrap(true);
        wordDetails.setWrapStyleWord(true);
        wordDetails.setEditable(false);



        // Add index button action listener
        indexButton.addActionListener(e -> {
            indexTrendingWords();  // Call method to index words
            wordListModel.clear();  // Clear previous words

            // Add the new words and their counts to the list model (sorted by count)
            wordCountMap.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue() - e1.getValue())  // Sort by count (descending)
                    .forEach(entry -> {
                        String wordWithCount = entry.getKey() + ": " + entry.getValue();  // Display word with count
                        wordListModel.addElement(wordWithCount);  // Add word + count to the list
                    });
        });

        // Handle word selection from the trending list
        trendingList.addListSelectionListener(e -> {
            String selectedItem = trendingList.getSelectedValue();
            if (selectedItem != null && !selectedItem.isEmpty()) {
                // Extract the word from the list item ("Word: Count" format, so split by ":")
                String selectedWord = selectedItem.split(":")[0].trim();

                // Get the count and videos associated with the word
                int count = wordCountMap.get(selectedWord);
                List<YouTubeVideo> videos = wordVideoMap.get(selectedWord);

                // Update the main video list (`videoList`) with videos that match the selected trending word
                DefaultListModel<String> videoListModel = new DefaultListModel<>();
                for (YouTubeVideo video : videos) {
                    videoListModel.addElement(video.getTitle());
                }
                videoList.setModel(videoListModel);  // Update the main video list with the filtered videos

                // Display the count and associated videos in the text area
                StringBuilder details = new StringBuilder("Word: " + selectedWord + "\n");
                details.append("Count: ").append(count).append("\nVideos:\n");
                for (YouTubeVideo video : videos) {
                    details.append(video.getTitle()).append("\n");
                }
                wordDetails.setText(details.toString());
            }
        });

        // Adding components to the panel
        trendingPanel.add(indexButton, BorderLayout.NORTH);  // Add index button at the top
        trendingPanel.add(trendingScrollPane, BorderLayout.CENTER);  // Add the trending list in the center

        return trendingPanel;
    }



    // Method to index the words from video titles and descriptions, excluding common words
    private void indexTrendingWords() {
        wordCountMap.clear();
        wordVideoMap.clear();

        // Define a set of common stop words to exclude
        Set<String> stopWords = Set.of(
                "of", "the", "and", "a", "an", "in", "on", "for", "to", "with", "by", "at",
                "is", "are", "was", "were", "it", "that", "this", "as", "from", "or", "but", "if", "be"
        );

        // Loop through all videos to index words
        for (YouTubeVideo video : videoData) {
            // Combine title and description into a single string and split into words
            String[] words = (video.getTitle() + " " + video.getDescription()).split("\\s+");

            for (String word : words) {
                // Clean up the word: convert to lowercase and remove punctuation
                word = word.toLowerCase().replaceAll("[^a-z]", "").trim();

                // Skip short words, empty strings, and stop words
                if (word.length() < 2 || stopWords.contains(word)) {
                    continue;
                }

                // Increment the word count
                wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);

                // Associate the word with the video
                wordVideoMap.computeIfAbsent(word, k -> new ArrayList<>()).add(video);
            }
        }
    }



    private void updateVideoDetails() {
        int selectedIndex = videoList.getSelectedIndex();
        if (selectedIndex != -1) {
            YouTubeVideo selectedVideo = videoData.get(selectedIndex);

            // Update UI components with video details
            titleLabel.setText(selectedVideo.getTitle());
            descriptionLabel.setText(selectedVideo.getDescription());

            // Update the table with dynamic data
            String[][] updatedData = {
                    {"Channel:", selectedVideo.getChannel()},
                    {"Date Posted:", selectedVideo.getDate()},
                    {"Category:", selectedVideo.getCategoryId()},
                    {"Live Broadcast:", selectedVideo.getLiveBroadcastContent()}
            };

            // Update the table model
            infoTable.setModel(new javax.swing.table.DefaultTableModel(updatedData, new String[]{"", ""}));

            // Update the thumbnail image
            try {
                ImageIcon thumbnail = new ImageIcon(new URL(selectedVideo.getThumbnailUrl()));
                Image scaledImage = thumbnail.getImage().getScaledInstance(250, 150, Image.SCALE_SMOOTH);
                thumbnailLabel.setIcon(new ImageIcon(scaledImage));
            } catch (MalformedURLException e) {
                thumbnailLabel.setText("Thumbnail Image Not Available");
            }

            // Call to update charts
            int maxLikes = getMaxValue("likeCount");
            int minLikes = getMinValue("likeCount");
            int maxComments = getMaxValue("commentCount");
            int minComments = getMinValue("commentCount");
            int maxViews = getMaxValue("viewCount");
            int minViews = getMinValue("viewCount");

            // Update bar charts
            updateBarChart(chartPanelLikes, "Likes", selectedVideo.getLikeCount(), minLikes, maxLikes);
            updateBarChart(chartPanelComments, "Comments", selectedVideo.getCommentCount(), minComments, maxComments);
            updateBarChart(chartPanelViews, "Views", selectedVideo.getViewCount(), minViews, maxViews);
        }
    }

    // Method to find maximum value across the video data for each chart type
    private int getMaxValue(String valueType) {
        int maxValue = 0;
        for (YouTubeVideo video : videoData) {
            int value;
            switch (valueType) {
                case "likeCount":
                    value = video.getLikeCount();
                    break;
                case "commentCount":
                    value = video.getCommentCount();
                    break;
                case "viewCount":
                    value = video.getViewCount();
                    break;
                default:
                    value = 0;
            }
            maxValue = Math.max(maxValue, value);
        }
        return maxValue;
    }

    // Method to find minimum value across the video data for each chart type
    private int getMinValue(String valueType) {
        int minValue = Integer.MAX_VALUE;
        for (YouTubeVideo video : videoData) {
            int value;
            switch (valueType) {
                case "likeCount":
                    value = video.getLikeCount();
                    break;
                case "commentCount":
                    value = video.getCommentCount();
                    break;
                case "viewCount":
                    value = video.getViewCount();
                    break;
                default:
                    value = Integer.MAX_VALUE;
            }
            minValue = Math.min(minValue, value);
        }
        return minValue;
    }

    // Create the right panel for video details and statistics
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());

        // Arrange components in the layout
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        JPanel createFileInputterPanel = createFileInputterPanel();
        JPanel sortingCriteriaPanel = createSortingCriteriaPanel();

        JPanel spacerPanel = new JPanel();
        spacerPanel.setPreferredSize(new Dimension(0, 10));
        topPanel.add(spacerPanel, BorderLayout.NORTH);

        topPanel.add(createFileInputterPanel, BorderLayout.CENTER);
        topPanel.add(sortingCriteriaPanel, BorderLayout.SOUTH);
        rightPanel.add(topPanel, BorderLayout.NORTH);

        //Video Details Panel (Top Right)
        JPanel videoDetailsPanel = createVideoDetailsPanel();
        rightPanel.add(videoDetailsPanel, BorderLayout.CENTER);

        // Statistics Panel (Bottom Right)
        JPanel statisticsPanel = createStatisticsPanel();
        rightPanel.add(statisticsPanel, BorderLayout.SOUTH);

        return rightPanel;
    }

    private JPanel createFileInputterPanel() {
        JPanel fileInputterPanel = new JPanel();
        fileInputterPanel.setLayout(new GridBagLayout());
        fileInputterPanel.setPreferredSize(new Dimension(800, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1; // Make the button fill the remaining space
        // Create text field to display the selected file path
        JTextField textField = new JTextField(30);
        textField.setText(jsonFileLocation);
        fileInputterPanel.add(textField, gbc);

        gbc.gridx = 1; // Move to the next column
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create load button to trigger file selection
        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent  e) {
                jsonFileLocation = textField.getText();
                System.out.println("loading data from : "+ jsonFileLocation);
                videoData = null;
                loadData();
            }
        });
        fileInputterPanel.add(loadButton, gbc);

        return fileInputterPanel;
    }

    // Create the Sorting Criteria Panel
    private JPanel createSortingCriteriaPanel() {
        JPanel sortingCriteriaPanel = new JPanel();
        sortingCriteriaPanel.setLayout(new GridBagLayout());
        sortingCriteriaPanel.setPreferredSize(new Dimension(800, 50));
        sortingCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Sorting Criteria"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Create radio buttons
        channelRadioButton = new JRadioButton("Channel");
        dateRadioButton = new JRadioButton("Date");
        viewRadioButton = new JRadioButton("View");
        descriptionRadioButton = new JRadioButton("Description");

        // Create a button group to ensure only one radio button can be selected at a time
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(channelRadioButton);
        buttonGroup.add(dateRadioButton);
        buttonGroup.add(viewRadioButton);
        buttonGroup.add(descriptionRadioButton);

        // Add radio buttons to the panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        sortingCriteriaPanel.add(channelRadioButton, gbc);
        gbc.gridx = 1;
        sortingCriteriaPanel.add(dateRadioButton, gbc);
        gbc.gridx = 2;
        sortingCriteriaPanel.add(viewRadioButton, gbc);
        gbc.gridx = 3;
        sortingCriteriaPanel.add(descriptionRadioButton, gbc);

        // Add an action listener to handle radio button selection changes
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Selected sorting criteria: " + getSelectedCriteria());
                sortingCriteria = getSelectedCriteria();
                loadData();
            }
        };
        channelRadioButton.addActionListener(actionListener);
        dateRadioButton.addActionListener(actionListener);
        viewRadioButton.addActionListener(actionListener);
        descriptionRadioButton.addActionListener(actionListener);

        return sortingCriteriaPanel;
    }

    private String getSelectedCriteria() {
        if (channelRadioButton.isSelected()) {
            return "Channel";
        } else if (dateRadioButton.isSelected()) {
            return "Date";
        } else if (viewRadioButton.isSelected()) {
            return "View";
        } else if (descriptionRadioButton.isSelected()) {
            return "Description";
        } else {
            return null;
        }
    }

    // Create the Video Details Panel
    private JPanel createVideoDetailsPanel() {
        JPanel videoDetailsPanel = new JPanel();
        videoDetailsPanel.setLayout(new GridBagLayout());
        videoDetailsPanel.setBorder(BorderFactory.createTitledBorder("Video Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;  // Allow horizontal stretching

        // Title
        JLabel titleLabelHeader = new JLabel("Title");
        titleLabel = new JLabel("YouTube Video Title");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        videoDetailsPanel.add(titleLabelHeader, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;  // Allow label to grow horizontally
        videoDetailsPanel.add(titleLabel, gbc);

        // Thumbnail
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;  // Reset weightx
        JLabel thumbnailHeader = new JLabel("Thumbnail");
        videoDetailsPanel.add(thumbnailHeader, gbc);
        gbc.gridx = 1;
        thumbnailLabel = new JLabel("Thumbnail Image");
        thumbnailLabel.setPreferredSize(null);  // Remove fixed size
        videoDetailsPanel.add(thumbnailLabel, gbc);

        // Description with vertical ScrollPane
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel descriptionHeader = new JLabel("Description");
        videoDetailsPanel.add(descriptionHeader, gbc);
        gbc.gridx = 1;

        // Create a scrollable text area for description with vertical scrolling only
        descriptionLabel = new JTextArea(5, 30);  // Use JTextArea for better scrolling
        descriptionLabel.setLineWrap(true);  // Wrap text
        descriptionLabel.setWrapStyleWord(true);  // Wrap at word boundaries
        descriptionLabel.setEditable(false);  // Make it non-editable

        JScrollPane descriptionScrollPane = new JScrollPane(descriptionLabel);
        descriptionScrollPane.setPreferredSize(null);  // Remove fixed size
        descriptionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        descriptionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        gbc.weightx = 1;
        gbc.weighty = 1;  // Allow description to take more vertical space
        gbc.fill = GridBagConstraints.BOTH;  // Allow the component to grow in both directions
        videoDetailsPanel.add(descriptionScrollPane, gbc);

        // Other Info Section (Channel, Date Posted, Category, Live Broadcast Content)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;  // Reset fill to horizontal
        JLabel otherHeader = new JLabel("Other");
        videoDetailsPanel.add(otherHeader, gbc);
        gbc.gridx = 1;
        otherPanel = createOtherInfoPanel();
        videoDetailsPanel.add(otherPanel, gbc);

        return videoDetailsPanel;
    }


    // Create the Other Information Section
    private JPanel createOtherInfoPanel() {
        JPanel otherPanel = new JPanel(new BorderLayout());

        // Initially empty table data
        String[][] data = {
                {"Channel:", ""},
                {"Date Posted:", ""},
                {"Category:", ""},
                {"Live Broadcast:", ""}
        };

        infoTable = new JTable(data, new String[]{"", ""});
        infoTable.setRowHeight(38);
        infoTable.setEnabled(false);
        infoTable.setShowGrid(true);
        infoTable.setGridColor(Color.GRAY);
        infoTable.setTableHeader(null);

        infoTable.setPreferredSize(new Dimension(400, 150));
        otherPanel.add(infoTable, BorderLayout.CENTER);

        return otherPanel;
    }

    // Create the Statistics Panel for the bar charts
    private JPanel createStatisticsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridBagLayout());  // Use GridBagLayout for better control
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;  // Allow components to grow both horizontally and vertically
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1;  // Allow panels to take equal horizontal space
        gbc.weighty = 1;  // Allow vertical growth if needed

        // Add the "Likes" chart panel
        chartPanelLikes = createChartPanel("Likes", new Color(0, 153, 76));
        gbc.gridx = 0;
        statsPanel.add(chartPanelLikes, gbc);

        // Add the "Comments" chart panel
        chartPanelComments = createChartPanel("Comments", new Color(0, 102, 204));
        gbc.gridx = 1;
        statsPanel.add(chartPanelComments, gbc);

        // Add the "Views" chart panel
        chartPanelViews = createChartPanel("Views", new Color(204, 102, 0));
        gbc.gridx = 2;
        statsPanel.add(chartPanelViews, gbc);

        return statsPanel;
    }


    // Helper method to create individual chart panels
    private JPanel createChartPanel(String title, Color barColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setPreferredSize(new Dimension(250, 200));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(0, "", title);

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset);
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, barColor);
        renderer.setMaximumBarWidth(0.1);

        ChartPanel chartPanel = new ChartPanel(chart);
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    // Update bar chart values dynamically and move value below the chart
    private void updateBarChart(JPanel chartPanel, String category, int value, double minValue, double maxValue) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(value, "", category);

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset,
                org.jfree.chart.plot.PlotOrientation.VERTICAL, false, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setMaximumBarWidth(0.1);

        // Assign unique colors for each chart
        if (category.equals("Likes")) {
            renderer.setSeriesPaint(0, new Color(76, 153, 0));  // Unique green for Likes
        } else if (category.equals("Comments")) {
            renderer.setSeriesPaint(0, new Color(0, 102, 255));  // Unique blue for Comments
        } else if (category.equals("Views")) {
            renderer.setSeriesPaint(0, new Color(255, 102, 0));  // Unique orange for Views
        }

        // Set the Y-axis range based on minValue and maxValue
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(minValue, maxValue);

        ChartPanel newChartPanel = new ChartPanel(chart);

        chartPanel.removeAll();
        chartPanel.add(newChartPanel, BorderLayout.CENTER);

        JLabel valueLabel = new JLabel(category + ": " + value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        chartPanel.add(valueLabel, BorderLayout.SOUTH);
        chartPanel.revalidate();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            YouTubeTrenderFrame frame = new YouTubeTrenderFrame();
            frame.setVisible(true);
        });
    }
}
