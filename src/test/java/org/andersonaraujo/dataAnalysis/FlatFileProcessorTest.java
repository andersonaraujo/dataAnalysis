package org.andersonaraujo.dataAnalysis;

import org.andersonaraujo.dataAnalysis.test.util.TestUtil;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/*n
 * Test class for {@link FlatFileProcessor}.
 *
 * @author Anderson Araujo.
 */
public class FlatFileProcessorTest {

    public static final String SALESMAN_DATA = "001ç1234567891234çDiegoç50000\n001ç3245678865434çRenatoç40000.99\n";
    public static final String CLIENT_DATA = "002ç2345675434544345çJosedaSilvaçRural\n002ç2345675433444345çEduardoPereiraçRural\n";
    public static final String SALES_DATA = "003ç10ç[1-10-100,2-30-2.50,3-40-3.10]çDiego\n003ç08ç[1-34-10,2-33-1.50,3-40-0.10]çRenato\n";
    public static final String INVALID_DATA = "001\n";

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1);


    @Test
    public void testOffNominalProcessFileWhenDoesNotExist() throws Exception {
        Callable<Boolean> processor = new FlatFileProcessor("file.dat", "/invalid/path/to/", "/invalid/path/to/");
        Future<Boolean> result = EXECUTOR.submit(processor);
        assertFalse("File should not be processed.", result.get());
    }

    @Test
    public void testOffNominalProcessFileIsNotDat() throws Exception {
        File tempFile = File.createTempFile("temp-file", ".txt");

        String path = pathToFile(tempFile);
        Callable<Boolean> processor = new FlatFileProcessor(tempFile.getName(), path, path);
        Future<Boolean> result = EXECUTOR.submit(processor);

        assertFalse("File should not be processed.", result.get());
    }

    @Test
    public void testOffNominalProcessFileInvalidFormat() throws Exception {
        File tempFile = File.createTempFile("temp-file", ".dat");
        writeToFile(tempFile, INVALID_DATA);

        String path = pathToFile(tempFile);
        Callable<Boolean> processor = new FlatFileProcessor(tempFile.getName(), path, path);
        Future<Boolean> result = EXECUTOR.submit(processor);

        assertFalse("File should not be processed.", result.get());
    }

    @Test
    public void testNominalProcessSalesmanLines() throws Exception {
        File tempFile = File.createTempFile("temp-file", ".dat");
        writeToFile(tempFile, SALESMAN_DATA);

        String path = pathToFile(tempFile);
        Callable<Boolean> processor = new FlatFileProcessor(tempFile.getName(), path, path);
        Future<Boolean> result = EXECUTOR.submit(processor);

        validate(processor, result.get(), 2, -1, null, null);
    }

    @Test
    public void testNominalProcessClientLines() throws Exception {
        File tempFile = File.createTempFile("temp-file", ".dat");
        writeToFile(tempFile, CLIENT_DATA);

        String path = pathToFile(tempFile);
        Callable<Boolean> processor = new FlatFileProcessor(tempFile.getName(), path, path);
        Future<Boolean> result = EXECUTOR.submit(processor);

        validate(processor, result.get(), -1, 2, null, null);
    }

    @Test
    public void testNominalProcessSalesLines() throws Exception {
        File tempFile = File.createTempFile("temp-file", ".dat");
        writeToFile(tempFile, SALES_DATA);

        String path = pathToFile(tempFile);
        Callable<Boolean> processor = new FlatFileProcessor(tempFile.getName(), path, path);
        Future<Boolean> result = EXECUTOR.submit(processor);

        validate(processor, result.get(), -1, -1, "10", "Renato");
    }

    /**
     * Nominal test when there are more than one sale for the same salesman.
     */
    @Test
    public void testNominalProcessSalesLinesWhenThereAreMoreThanOneForSameSalesman() throws Exception {
        File tempFile = File.createTempFile("temp-file", ".dat");
        writeToFile(tempFile, SALES_DATA + SALES_DATA);

        String path = pathToFile(tempFile);
        Callable<Boolean> processor = new FlatFileProcessor(tempFile.getName(), path, path);
        Future<Boolean> result = EXECUTOR.submit(processor);

        validate(processor, result.get(), -1, -1, "10", "Renato");
    }

    @Test
    public void testNominalProcessCompleteFile() throws Exception {
        File tempFile = File.createTempFile("temp-file", ".dat");
        writeToFile(tempFile, SALESMAN_DATA + CLIENT_DATA + SALES_DATA);

        String path = pathToFile(tempFile);
        Callable<Boolean> processor = new FlatFileProcessor(tempFile.getName(), path, path);
        Future<Boolean> result = EXECUTOR.submit(processor);

        validate(processor, result.get(), 2, 2, "10", "Renato");
    }

    @Test
    public void testNominalGenerateOutput() throws Exception {
        File tempFile = File.createTempFile("temp-file", ".dat");
        writeToFile(tempFile, SALESMAN_DATA + CLIENT_DATA + SALES_DATA);

        String path = pathToFile(tempFile);
        FlatFileProcessor processor = new FlatFileProcessor(tempFile.getName(), path, path);
        Future<Boolean> result = EXECUTOR.submit(processor);
        assertTrue("File should be processed.", result.get());

        String expectedOutput = "001çAmountClientsç2\n" +
                "002çAmountSalesmanç2\n" +
                "003çMostExpensiveSaleç10\n" +
                "004çWorstSalesmançRenato\n";
        String output = processor.generateOutput();
        assertEquals("Invalid output generated.", expectedOutput, output);
    }


    private void validate(Callable<Boolean> processorCallable, boolean result, int expectedAmountOfSalesman,
                          int expectedAmountOfClients, String expectedIdMostExpensiveSales,
                          String expectedWorstSalesman) throws Exception {

        FlatFileProcessor processor = (FlatFileProcessor) processorCallable;

        assertTrue("File should be processed.", result);

        if (expectedAmountOfSalesman >= 0) {
            Set salesmanCpfs = TestUtil.getFieldOnObject(processor, "salesmanCpfs", Set.class);
            assertEquals("Invalid amount of Salesman.", expectedAmountOfSalesman, salesmanCpfs.size());
        }

        if (expectedAmountOfClients >= 0) {
            Set clientsCnpjs = TestUtil.getFieldOnObject(processor, "clientsCnpjs", Set.class);
            assertEquals("Invalid amount of Clients.", expectedAmountOfClients, clientsCnpjs.size());
        }

        if (expectedIdMostExpensiveSales != null) {
            String mostExpensiveSaleId = TestUtil.getFieldOnObject(processor, "mostExpensiveSaleId", String.class);
            assertEquals("Invalid most expensive sales id.", expectedIdMostExpensiveSales, mostExpensiveSaleId);
        }

        if (expectedWorstSalesman != null) {
            @SuppressWarnings("unchecked")
            Map<String, BigDecimal> salesmanSales = TestUtil.getFieldOnObject(processor, "salesmanSales", Map.class);

            Comparator<Map.Entry<String, BigDecimal>> byValue = (entry1, entry2) -> entry1.getValue().compareTo(
                    entry2.getValue());
            Optional<Map.Entry<String, BigDecimal>> worst = salesmanSales
                    .entrySet()
                    .stream()
                    .sorted(byValue)
                    .findFirst();

            assertEquals("Invalid most expensive sales id.", expectedWorstSalesman, worst.get().getKey());
        }
    }

    private void writeToFile(File file, String value) throws IOException {
        Files.write(Paths.get(file.getAbsolutePath()), value.getBytes());
    }

    private static String pathToFile(File file) {
        String absolutePath = file.getAbsolutePath();
        return absolutePath.substring(0, absolutePath.lastIndexOf(File.separator)) + File.separator;
    }

}
