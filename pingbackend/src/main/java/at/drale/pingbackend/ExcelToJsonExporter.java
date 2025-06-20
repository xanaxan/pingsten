package at.drale.pingbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ExcelToJsonExporter {

    // Column indices (0-based)
    private static final int COL_NAME = 0;
    private static final int COL_IP1 = 1;
    private static final int COL_IP2 = 3;

    public static List<NameIps> readExcel(String excelPath) throws IOException {
        List<NameIps> entries = new ArrayList<>();
        try (InputStream is = Files.newInputStream(Paths.get(excelPath));
            Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;
            for (Row row : sheet) {
                if (firstRow) { // skip header
                    firstRow = false;
                    continue;
                }
                String name = getCellValue(row.getCell(COL_NAME));
                String ip1 = getCellValue(row.getCell(COL_IP1));
                String ip2 = getCellValue(row.getCell(COL_IP2));
                entries.add(new NameIps(name, ip1, ip2));
            }
        }
        return entries;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf(cell.getNumericCellValue());
        return cell.toString();
    }

    public static void writeJson(List<NameIps> entries, String jsonPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(Paths.get(jsonPath).toFile(), entries);
    }

    public static void main(String[] args) throws IOException {
        String excelFile = "L:\\programming\\java\\git-repo\\pingbackend\\IPs Work.xlsx"; // path to your Excel file
        String jsonFile = "output.json"; // path to save JSON
        List<NameIps> entries = readExcel(excelFile);
        writeJson(entries, jsonFile);
        System.out.println("Exported to " + jsonFile);
    }
}