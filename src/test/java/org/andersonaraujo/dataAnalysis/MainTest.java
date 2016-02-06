package org.andersonaraujo.dataAnalysis;

import org.andersonaraujo.dataAnalysis.test.util.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.file.FileSystems;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link Main}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({System.class, FileSystems.class, Main.class})
public class MainTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test()
    public void testOffNominalSetDirectoriesWhenDirectoryDoesNotExist() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(Main.ENV_VAR_HOMEPATH_NOT_CREATED_ERROR_MSG);

        PowerMockito.mockStatic(System.class);
        when(System.getenv(Main.ENV_VAR_NAME)).thenReturn(null);

        Main main = new Main();
        main.setDirectories();
    }

    @Test
    public void testNominalSetDirectories() throws Exception {
        PowerMockito.mockStatic(System.class);
        when(System.getenv(Main.ENV_VAR_NAME)).thenReturn("/path/");

        Main main = new Main();
        main.setDirectories();

        String fullInputPath = TestUtil.getFieldOnObject(main, "fullInputPath", String.class);
        String fullOutputPath = TestUtil.getFieldOnObject(main, "fullOutputPath", String.class);
        assertEquals("/path//data/in/", fullInputPath);
        assertEquals("/path//data/out/", fullOutputPath);
    }

}


