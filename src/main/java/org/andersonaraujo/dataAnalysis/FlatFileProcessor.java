package org.andersonaraujo.dataAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * A {@link Callable} class to process a flat file asynchronously.
 * <p>
 * It will return {@link Boolean#TRUE} if the file is processed successfully or {@link Boolean#FALSE} if an error occur.
 * <p>
 * For information about the file input and output formats, please refer to the {@code README} file.
 *
 * @author Anderson Araujo.
 */
public class FlatFileProcessor implements Callable<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(FlatFileProcessor.class);

    private static final String VALID_EXTENSION = "dat";

    private static final String DELIMITER = "ç";

    private static final String LINE_BREAK = "\n";

    private static final String INPUT_TYPE_SALESMAN = "001";
    private static final String INPUT_TYPE_CUSTOMER = "002";
    private static final String INPUT_TYPE_SALES = "003";

    private static final String OUTPUT_TYPE_AMOUNT_CLIENT = "001";
    private static final String OUTPUT_TYPE_AMOUNT_SALESMAN = "002";
    private static final String OUTPUT_TYPE_EXPENSIVE_SALE = "003";
    private static final String OUTPUT_TYPE_WORST_SALESMAN = "004";
    private static final String OUTPUT_AMOUNT_CLIENTS = "AmountClients";
    private static final String OUTPUT_AMOUNT_SALESMAN = "AmountSalesman";
    private static final String OUTPUT_MOST_EXPENSIVE_SALE = "MostExpensiveSale";
    private static final String OUTPUT_WORST_SALESMAN = "WorstSalesman";

    /**
     * The name to the file.
     */
    private final String fileName;

    /**
     * The input directory path.
     */
    private final String inputDirectory;

    /**
     * The output directory path.
     */
    private final String outputDirectory;

    /**
     * Holds the Client's CNPJs.
     * As the requirement is not clear whether the client records will be sorted or can be repeated,
     * this Set will hold all unique CNPJs and be used to define the amount of clients.
     */
    private final Set<String> clientsCnpjs = new HashSet<>();

    /**
     * Holds the Salesman's CPFs.
     * <p>
     * As the requirement is not clear whether the salesman records will be sorted or can be repeated,
     * this Set will hold all unique CPFs and be used to define the amount of salesman.
     */
    private final Set<String> salesmanCpfs = new HashSet<>();

    /**
     * Holds the Salesman's name as key and the sum of sales.
     * <p>
     * As the requirement is not clear whether the sales records will be sorted,
     * this map will be used to hold all Salesman's information to define the worst salesman
     * (the one that the sum of sales is lower).
     */
    private final Map<String, BigDecimal> salesmanSales = new HashMap<>();

    /**
     * Holds the value of the most expensive sale.
     */
    private BigDecimal mostExpensiveSaleValue = BigDecimal.ZERO;

    /**
     * Holds the ID of the most expensive sale.
     */
    private String mostExpensiveSaleId;


    public FlatFileProcessor(String fileName, String inputDirectory, String outputDirectory) {
        this.fileName = fileName;
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        logger.debug("New instance created for file '{}'.", fileName);
    }

    @Override
    public Boolean call() throws Exception {
        return processFile();
    }

    /**
     * Process the file (read input and write output).
     *
     * @return True if the file has been processed correctly. False otherwise.
     */
    Boolean processFile() {
        if (!isValid()) {
            return Boolean.FALSE;
        }

        // Reads the file line by line
        try (Stream<String> stream = Files.lines(Paths.get(fullPathToInputFile()))) {
            stream
                    .forEach(this::processLine);

            writeOutput();

            return Boolean.TRUE;

        } catch (Exception e) {
            logger.error(String.format("Error occurred while processing the file '%s'.", fileName), e);
            return Boolean.FALSE;
        }
    }

    void processLine(String line) {
        StringTokenizer stringTokenizer = new StringTokenizer(line, DELIMITER);
        String kind = stringTokenizer.nextToken();

        switch (kind) {
            case INPUT_TYPE_SALESMAN:
                processSalesmanLine(stringTokenizer);
                break;

            case INPUT_TYPE_CUSTOMER:
                processCustomerLine(stringTokenizer);
                break;

            case INPUT_TYPE_SALES:
                processSalesLine(stringTokenizer);
                break;

            default:
                break;
        }
    }

    /**
     * Writes the output to the output file.
     * <p>
     * The output file will be save at the same location as the input file.
     * The only difference will be the extension, instead of .dat, it will be .done.dot.
     *
     * @throws IOException
     */
    private void writeOutput() throws IOException {
        String outputFileName = fileName.substring(0, fileName.lastIndexOf(Main.INPUT_FILE_EXTENSION))
                + Main.OUTPUT_FILE_EXTENSION;
        String output = generateOutput();
        Files.write(Paths.get(outputDirectory + outputFileName), output.getBytes());
    }

    /**
     * Generates the text to be saved to the output file.
     *
     * @return The text output.
     */
    String generateOutput() {
        return OUTPUT_TYPE_AMOUNT_CLIENT
                + DELIMITER
                + OUTPUT_AMOUNT_CLIENTS
                + DELIMITER
                + clientsCnpjs.size()
                + LINE_BREAK

                + OUTPUT_TYPE_AMOUNT_SALESMAN
                + DELIMITER
                + OUTPUT_AMOUNT_SALESMAN
                + DELIMITER
                + salesmanCpfs.size()
                + LINE_BREAK

                + OUTPUT_TYPE_EXPENSIVE_SALE
                + DELIMITER
                + OUTPUT_MOST_EXPENSIVE_SALE
                + DELIMITER
                + mostExpensiveSaleId
                + LINE_BREAK

                + OUTPUT_TYPE_WORST_SALESMAN
                + DELIMITER
                + OUTPUT_WORST_SALESMAN
                + DELIMITER
                + getWorstSalesman()
                + LINE_BREAK;
    }

    private void processSalesmanLine(StringTokenizer lineTokens) {
        this.salesmanCpfs.add(lineTokens.nextToken());
    }

    private void processCustomerLine(StringTokenizer lineTokens) {
        this.clientsCnpjs.add(lineTokens.nextToken());
    }

    private void processSalesLine(StringTokenizer lineTokens) {
        String salesId = lineTokens.nextToken();
        String items = lineTokens.nextToken();
        String salesman = lineTokens.nextToken();

        BigDecimal salesValue = sumItems(items);
        if (salesValue.compareTo(mostExpensiveSaleValue) == 1) {
            mostExpensiveSaleId = salesId;
            mostExpensiveSaleValue = salesValue;
        }

        BigDecimal currentSalesmanSum = this.salesmanSales.get(salesman);
        if (currentSalesmanSum == null) {
            currentSalesmanSum = salesValue;
        } else {
            currentSalesmanSum = currentSalesmanSum.add(salesValue);
        }
        salesmanSales.put(salesman, currentSalesmanSum);
    }

    private BigDecimal sumItems(String items) {
        String[] itemsArray = items.replace("[", "").replace("]", "").split(",");
        BigDecimal sum = BigDecimal.ZERO;

        for (String itemString : itemsArray) {
            String[] itemArray = itemString.split("-");
            if (!itemArray[2].isEmpty()) {
                sum = sum.add(new BigDecimal(itemArray[2]));
            }
        }

        return sum;
    }

    private String getWorstSalesman() {
        Comparator<Map.Entry<String, BigDecimal>> byValue = (entry1, entry2) -> entry1.getValue().compareTo(
                entry2.getValue());
        Optional<Map.Entry<String, BigDecimal>> worst = salesmanSales
                .entrySet()
                .stream()
                .sorted(byValue)
                .findFirst();

        if (worst.isPresent()) {
            return worst.get().getKey();
        } else {
            return null;
        }
    }

    /**
     * Perform the following validations:
     * <ul>
     * <li>The file exists;</li>
     * <li>The file has the correct extension {@link FlatFileProcessor#VALID_EXTENSION}.</li>
     * </ul>
     *
     * @return True if the file is valid. False otherwise.
     */
    private boolean isValid() {
        File file = new File(fullPathToInputFile());

        // Verifies the file exists
        if (!file.exists()) {
            logger.error("File '{}' doesn't exist.", fileName);
            return false;
        }

        // Verifies the file has the correct extension
        String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        if (!VALID_EXTENSION.equalsIgnoreCase(extension)) {
            logger.error("File '{}' has an invalid extension.", fileName);
            return false;
        }

        return true;
    }

    private String fullPathToInputFile() {
        return inputDirectory + fileName;
    }

}
