/*
 * Created on 29/lug/2011
 * Copyright 2010 by Andrea Vacondio (andrea.vacondio@gmail.com).
 * 
 * This file is part of the Sejda source code
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sejda.core.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.sejda.TestUtils.encryptedAtRest;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.sejda.model.optimization.OptimizationPolicy;
import org.sejda.model.output.ExistingOutputPolicy;
import org.sejda.model.parameter.SimpleSplitParameters;
import org.sejda.model.pdf.PdfVersion;
import org.sejda.model.pdf.page.PredefinedSetOfPages;
import org.sejda.sambox.cos.COSName;
import org.sejda.sambox.pdmodel.PDResources;
import org.sejda.sambox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.sejda.sambox.pdmodel.graphics.state.PDSoftMask;

/**
 * @author Andrea Vacondio
 * 
 */
@Ignore
public abstract class SimpleSplitTaskTest extends BaseTaskTest<SimpleSplitParameters> {

    private SimpleSplitParameters parameters;

    private void setUpParameters(PredefinedSetOfPages type) {
        parameters = new SimpleSplitParameters(type);
        parameters.setCompress(true);
        parameters.setVersion(PdfVersion.VERSION_1_6);
        parameters.addSource(shortInput());
        parameters.setExistingOutputPolicy(ExistingOutputPolicy.OVERWRITE);
    }

    @Test
    public void testExecuteBurst() throws IOException {
        setUpParameters(PredefinedSetOfPages.ALL_PAGES);
        testContext.directoryOutputTo(parameters);
        execute(parameters);
        testContext.assertTaskCompleted();
        testContext.assertOutputSize(4);
    }

    @Test
    public void testEncryptionAtRest() throws IOException {
        parameters = new SimpleSplitParameters(PredefinedSetOfPages.ALL_PAGES);
        parameters.addSource(encryptedAtRest(shortInput()));
        parameters.setExistingOutputPolicy(ExistingOutputPolicy.OVERWRITE);
        testContext.directoryOutputTo(parameters);

        execute(parameters);
        testContext.assertTaskCompleted();
        testContext.assertOutputSize(4);
    }

    @Test
    public void testExecuteBurstEncrypted() throws IOException {
        setUpParameters(PredefinedSetOfPages.ALL_PAGES);
        parameters.removeAllSources();
        parameters.addSource(stronglyEncryptedInput());
        testContext.directoryOutputTo(parameters);
        execute(parameters);
        testContext.assertTaskCompleted();
        testContext.assertOutputSize(4);
    }

    @Test
    public void testExecuteEven() throws IOException {
        setUpParameters(PredefinedSetOfPages.EVEN_PAGES);
        testContext.directoryOutputTo(parameters);
        execute(parameters);
        testContext.assertTaskCompleted();
        testContext.assertOutputSize(2);
    }

    @Test
    public void testExecuteOdd() throws IOException {
        setUpParameters(PredefinedSetOfPages.ODD_PAGES);
        testContext.directoryOutputTo(parameters);
        execute(parameters);
        testContext.assertTaskCompleted();
        testContext.assertOutputSize(3);
    }

    @Test
    public void batchMode() throws IOException {
        setUpParameters(PredefinedSetOfPages.EVEN_PAGES);
        parameters.addSource(shortInput());
        parameters.addSource(mediumInput());

        testContext.directoryOutputTo(parameters);
        execute(parameters);

        testContext.assertTaskCompleted();
        testContext.assertOutputSize(19);
        testContext.assertOutputContainsFilenames("1_short-test-file.pdf", "3_short-test-file.pdf",
                "1_medium-test-file.pdf", "3_medium-test-file.pdf", "5_medium-test-file.pdf", "7_medium-test-file.pdf",
                "9_medium-test-file.pdf", "11_medium-test-file.pdf", "13_medium-test-file.pdf",
                "15_medium-test-file.pdf", "17_medium-test-file.pdf", "19_medium-test-file.pdf",
                "21_medium-test-file.pdf", "23_medium-test-file.pdf", "25_medium-test-file.pdf",
                "27_medium-test-file.pdf", "29_medium-test-file.pdf", "31_medium-test-file.pdf",
                "33_medium-test-file.pdf");
    }

    @Test
    // make sure we don't loose the mask in case of shared pages resources and shared mask/forms resources, optimization triggered.
    public void noDataLoss() throws IOException {
        parameters = new SimpleSplitParameters(PredefinedSetOfPages.ALL_PAGES);
        parameters.setCompress(true);
        parameters.setVersion(PdfVersion.VERSION_1_6);
        parameters.addSource(customInput("pdf/shared_res_form_extgs_softmask.pdf"));
        parameters.setExistingOutputPolicy(ExistingOutputPolicy.OVERWRITE);
        parameters.setOutputPrefix("[FILENUMBER]-chuck");
        parameters.setOptimizationPolicy(OptimizationPolicy.AUTO);
        testContext.directoryOutputTo(parameters);
        execute(parameters);
        testContext.assertTaskCompleted();
        testContext.assertOutputSize(2).forPdfOutput("1-chuck.pdf", d -> {
            try {
                PDResources pageRes = d.getPage(0).getResources();
                assertTrue(pageRes.isImageXObject(COSName.getPDFName("Im1")));
                assertTrue(pageRes.isFormXObject(COSName.getPDFName("Form2")));
                assertFalse(pageRes.isImageXObject(COSName.getPDFName("Im3")));
                assertNull(pageRes.getExtGState(COSName.getPDFName("gs2")));
                PDExtendedGraphicsState gs1 = pageRes.getExtGState(COSName.getPDFName("gs1"));
                assertNotNull(gs1);
                PDSoftMask softMask = gs1.getSoftMask();
                assertNotNull(softMask);
                PDResources maskRes = softMask.getGroup().getResources();
                assertTrue(maskRes.isImageXObject(COSName.getPDFName("Im1")));
                assertTrue(maskRes.isImageXObject(COSName.getPDFName("Im2")));
                assertFalse(maskRes.isImageXObject(COSName.getPDFName("Im3")));
            } catch (IOException e) {
                fail();
            }
        });
    }

    @Test
    public void specificResultFilenames() throws IOException {
        setUpParameters(PredefinedSetOfPages.ALL_PAGES);
        testContext.directoryOutputTo(parameters);
        parameters.addSpecificResultFilename("one");
        parameters.addSpecificResultFilename("two");
        parameters.addSpecificResultFilename("some/*?Invalid<chars");
        execute(parameters);

        testContext.assertTaskCompleted();
        testContext.assertOutputSize(4).assertOutputContainsFilenames("one.pdf", "two.pdf", "someInvalidchars.pdf");
    }

}
