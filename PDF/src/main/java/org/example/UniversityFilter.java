package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UniversityFilter {

    public static void main(String[] args) {
        // 1. Считываем номер университета с консоли
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите номер университета: ");
        String universityNumber = scanner.nextLine().trim();

        // 2. Задаём путь к PDF-файлу
        String pdfFilePath = "\"C:\\Users\\Жасулан\\OneDrive\\Рабочий стол\\grants.pdf\"";

        // Список, куда сложим все подходящие строки
        List<String> matchedLines = new ArrayList<>();

        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
            // 3. Извлекаем текст из PDF
            PDFTextStripper stripper = new PDFTextStripper();

            // (Дополнительно можно включить сортировку по позиции, чтобы текст был более «табличным»)
            // stripper.setSortByPosition(true);

            String text = stripper.getText(document);

            // 4. Разбиваем на строки
            String[] lines = text.split("\\r?\\n");

            // 5. Ищем нужные строки
            for (String line : lines) {
                // Пропускаем совсем пустые
                if (line.trim().isEmpty()) {
                    continue;
                }
                // Можно пропустить строки, которые начинаются с "№" (заголовки)
                if (line.trim().startsWith("№")) {
                    continue;
                }

                // Разделяем строку по пробелам (одному или нескольким)
                String[] columns = line.trim().split("\\s+");

                // Если мало столбцов, переходим к следующей
                if (columns.length < 2) {
                    continue;
                }

                // Предположим, что "ВУЗ" находится в самом последнем столбце
                // (для строки вида "1 001175243 ХАЙРУЛЛИНА ДИЛЬНАЗ ЕРБОЛАТОВНА 137 013", это columns[6])
                // но из-за многострочных ФИО индекс может «съезжать».
                // Тогда берём последний элемент массива:
                String universityColumn = columns[columns.length - 1].trim();

                // Сравниваем с номером, введённым пользователем
                if (universityColumn.equals(universityNumber)) {
                    matchedLines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении PDF файла: " + e.getMessage());
        } finally {
            scanner.close();
        }

        // 6. Записываем результат в текстовый файл
        if (matchedLines.isEmpty()) {
            System.out.println("Не найдено ни одной строки с ВУЗ: " + universityNumber);
        } else {
            String outputFileName = "filtered_output.txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
                for (String matchedLine : matchedLines) {
                    writer.write(matchedLine);
                    writer.newLine();
                }
            } catch (IOException e) {
                System.out.println("Ошибка при записи в файл: " + e.getMessage());
            }
            System.out.println("Записано " + matchedLines.size() + " строк(и) в файл: " + outputFileName);
        }
    }
}
