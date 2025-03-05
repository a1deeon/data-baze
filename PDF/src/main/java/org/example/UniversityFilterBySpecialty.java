package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class UniversityFilterBySpecialty {
    public static void main(String[] args) {
        // 1. Запрашиваем код специальности
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите код специальности (например, 001): ");
        String specialtyCode = scanner.nextLine().trim();

        // Проверяем, что введены ровно три цифры
        if (!specialtyCode.matches("\\d{3}")) {
            System.out.println("Неверный формат. Нужно ввести ровно три цифры, например 001 или 075.");
            return;
        }

        // Сформируем заголовок специальности, который ищем в PDF, например: "B001 - "
        String specialtyMarker = "B" + specialtyCode + " - ";

        // Путь к PDF
        String pdfFilePath = "\"C:\\Users\\Жасулан\\OneDrive\\Рабочий стол\\grants.pdf\"";

        // Карта (университет -> список строк)
        Map<String, List<String>> universityToStudents = new LinkedHashMap<>();

        // Флаг: внутри нужного блока или нет
        boolean inSpecialtyBlock = false;

        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            // stripper.setSortByPosition(true); // при необходимости

            String fullText = stripper.getText(document);
            String[] lines = fullText.split("\\r?\\n");

            for (String line : lines) {
                String trimmed = line.trim();

                // Пропускаем пустые строки
                if (trimmed.isEmpty()) {
                    continue;
                }

                // Проверяем, не начинается ли строка с BXXX - (новый блок специальности)
                if (trimmed.startsWith("B") && trimmed.contains(" - ")) {
                    // Если это та самая специальность, переходим в её блок
                    if (trimmed.startsWith(specialtyMarker)) {
                        inSpecialtyBlock = true;
                    } else {
                        // Иначе выходим из блока
                        inSpecialtyBlock = false;
                    }
                    continue;
                }

                // Если мы не в нужном блоке, пропускаем строку
                if (!inSpecialtyBlock) {
                    continue;
                }

                // Проверяем, что строка выглядит как «№ ...», т.е. начинается с цифры
                if (!trimmed.matches("^\\d+.*")) {
                    // Если нет, это, скорее всего, служебная строка (параметры, заголовки и т.д.)
                    continue;
                }

                // Делим по пробелам
                String[] columns = trimmed.split("\\s+");
                if (columns.length < 5) {
                    continue;
                }

                // Последний элемент массива — это код университета
                String universityCode = columns[columns.length - 1].trim();

                // *** Добавляем проверку: исключаем, если ВУЗ не является числом ***
                // matches("\\d+") вернёт true, если вся строка - только цифры
                if (!universityCode.matches("\\d+")) {
                    // Пропускаем такие записи
                    continue;
                }

                // Если всё ок, добавляем в карту
                universityToStudents
                        .computeIfAbsent(universityCode, k -> new ArrayList<>())
                        .add(trimmed);
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении PDF: " + e.getMessage());
            return;
        } finally {
            scanner.close();
        }

        // 2. Выводим в консоль список (университет -> кол-во поступивших)
        if (universityToStudents.isEmpty()) {
            System.out.println("Не найдено ни одной записи для специальности B" + specialtyCode);
            return;
        }

        System.out.println("=== Университеты и количество поступивших (B" + specialtyCode + ") ===");
        for (Map.Entry<String, List<String>> entry : universityToStudents.entrySet()) {
            String uniCode = entry.getKey();
            int count = entry.getValue().size();
            System.out.println("Университет: " + uniCode + ", поступивших: " + count);
        }

        // 3. Записываем подробный список студентов в файл
        String outputFileName = "students_by_university_B" + specialtyCode + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
            for (Map.Entry<String, List<String>> entry : universityToStudents.entrySet()) {
                String uni = entry.getKey();
                List<String> students = entry.getValue();

                writer.write("=== ВУЗ " + uni + " ===");
                writer.newLine();

                for (String studentLine : students) {
                    writer.write(studentLine);
                    writer.newLine();
                }
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Ошибка при записи в файл: " + e.getMessage());
        }

        System.out.println("Детальный список сохранён в файл: " + outputFileName);
    }
}
