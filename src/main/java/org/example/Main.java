import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.stream.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.*;

// Класс, представляющий кошку с её характеристиками
class Cat {
    private String breed; // Порода кошки
    private String name; // Имя кошки
    private String size; // Размер кошки
    private String furColor; // Цвет шерсти
    private String eyeColor; // Цвет глаз
    private String gender; // Пол кошки

    // Конструктор для создания объекта кошки
    public Cat(String breed, String name, String size, String furColor, String eyeColor, String gender) {
        this.breed = breed;
        this.name = name;
        this.size = size;
        this.furColor = furColor;
        this.eyeColor = eyeColor;
        this.gender = gender;
    }

    // Геттер для породы
    public String getBreed() {
        return breed;
    }

//всем првиет

    // Геттер для пола
    public String getGender() {
        return gender;
    }

    // Переопределение метода toString для удобного вывода информации о кошке
    @Override
    public String toString() {
        return String.format("Cat [Breed=%s, Name=%s, Size=%s, FurColor=%s, EyeColor=%s, Gender=%s]",
                breed, name, size, furColor, eyeColor, gender);
    }
}

// Класс для работы с объектами кошек
class CatHandler {
    private List<Cat> cats; // Список объектов кошек

    // Конструктор, который загружает данные о кошках из CSV-файла
    public CatHandler(String filePath) throws IOException {
        if (!Files.exists(Paths.get(filePath))) {
            throw new FileNotFoundException("Файл не найден: " + filePath);
        }
        this.cats = loadCats(filePath); // Загрузка данных из файла
    }

    // Метод для загрузки данных о кошках из CSV-файла
    public List<Cat> loadCats(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath))
                .skip(1) // Пропускаем заголовок таблицы
                .map(line -> {
                    String[] data = line.split(",");
                    return new Cat(data[0], data[1], data[2], data[3], data[4], data[5]);
                })
                .collect(Collectors.toList());
    }

    // Метод для поиска самой распространённой породы кошек
    public String findMostCommonBreed() {
        return cats.stream()
                .collect(Collectors.groupingBy(Cat::getBreed, Collectors.counting())) // Группировка по породам
                .entrySet().stream()
                .max(Map.Entry.comparingByValue()) // Нахождение максимального значения
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    // Метод для получения распределения кошек по полу
    public Map<String, Long> getGenderDistribution() {
        return cats.stream()
                .collect(Collectors.groupingBy(Cat::getGender, Collectors.counting())); // Группировка по полу
    }

    // Метод для получения распределения по породам
    public Map<String, Long> getBreedDistribution() {
        return cats.stream()
                .collect(Collectors.groupingBy(Cat::getBreed, Collectors.counting())); // Группировка по породам
    }

    // Метод для сохранения данных о кошках в файл
    public void saveToFile(String filePath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            writer.write("Breed,Name,Size,FurColor,EyeColor,Gender\n");
            for (Cat cat : cats) {
                writer.write(String.join(",",
                        cat.getBreed(),
                        cat.getGender()) + "\n");
            }
        }
    }

    // Метод для получения списка кошек
    public List<Cat> getCats() {
        return this.cats;
    }
}

// Класс для работы с базой данных и таблицей "Cats"
class CatDatabase {
    private Connection connection; // Соединение с базой данных

    // Конструктор для подключения к базе данных
    public CatDatabase(String dbName) {
        try {
            Class.forName("org.sqlite.JDBC"); // Загрузка драйвера SQLite
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            System.out.println("Connection established!");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite Driver not found. Add the SQLite JDBC dependency to your project.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database.");
            e.printStackTrace();
        }
    }

    // Метод для создания таблицы Cats в базе данных
    public void createTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Cats (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    breed TEXT,
                    name TEXT,
                    size TEXT,
                    furColor TEXT,
                    eyeColor TEXT,
                    gender TEXT
                )""");
        }
    }

    // Метод для добавления данных о кошках в таблицу
    public void insertData(List<Cat> cats) throws SQLException {
        String sql = "INSERT INTO Cats (breed, name, size, furColor, eyeColor, gender) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Cat cat : cats) {
                pstmt.setString(1, cat.getBreed());
                pstmt.setString(2, cat.getGender());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    // Метод для выполнения запроса данных из таблицы Cats и их вывода
    public void queryData() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Cats")) {
            while (rs.next()) {
                System.out.println(
                        "Breed: " + rs.getString("breed") + ", " +
                                "Gender: " + rs.getString("gender"));
            }
        }
    }
}

// Основной класс для демонстрации работы программы
public class Main {
    public static void main(String[] args) {
        try {
            // Укажите полный путь к файлу
            String filePath = "C:/Users/uziar/IdeaProjects/java_pr/src/main/resources/cats_database_english.csv";

            // Проверка текущего рабочего каталога
            System.out.println("Current working directory: " + System.getProperty("user.dir"));

            // Инициализация обработчика кошек и загрузка данных
            CatHandler catHandler = new CatHandler(filePath);

            // Поиск самой распространённой породы
            System.out.println("Most common breed: " + catHandler.findMostCommonBreed());

            // Получение распределения по полу
            System.out.println("Gender distribution: " + catHandler.getGenderDistribution());

            // Сохранение данных в файл
            catHandler.saveToFile("output_cats.csv");

            // Инициализация базы данных
            CatDatabase db = new CatDatabase("cats.db");
            db.createTable();

            // Добавление данных в базу данных
            db.insertData(catHandler.getCats());

            // Создание круговой диаграммы с распределением пород
            Map<String, Long> breedDistribution = catHandler.getBreedDistribution();
            DefaultPieDataset dataset = new DefaultPieDataset();
            for (Map.Entry<String, Long> entry : breedDistribution.entrySet()) {
                // Добавляем породу и количество в данные для диаграммы
                dataset.setValue(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
            }

            JFreeChart chart = ChartFactory.createPieChart(
                    "Breed Distribution", // Заголовок
                    dataset,              // Данные
                    true,                 // Легенда
                    true,                 // Tooltips
                    false                 // URLs
            );

            // Отображение диаграммы
            JFrame frame = new JFrame("Cat Breed Distribution");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.add(new ChartPanel(chart));
            frame.setVisible(true);

        } catch (FileNotFoundException e) {
            System.err.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
