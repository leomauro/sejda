/*
 * Created on 03 feb 2017
 * Copyright 2017 by Andrea Vacondio (andrea.vacondio@gmail.com).
 * This file is part of Sejda.
 *
 * Sejda is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sejda is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Sejda.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sejda.cli.transformer;

import org.sejda.cli.model.PdfToPngTaskCliArguments;
import org.sejda.model.parameter.image.PdfToPngParameters;

/**
 * {@link CommandCliArgumentsTransformer} for the PdfToPngTask task command line interface
 * 
 * @author Andrea Vacondio
 */
public class PdfToPngCliArgumentsTransformer extends BaseCliArgumentsTransformer
        implements CommandCliArgumentsTransformer<PdfToPngTaskCliArguments, PdfToPngParameters> {

    /**
     * Transforms {@link PdfToPngTaskCliArguments} to {@link PdfToPngParameters}
     * 
     * @param taskCliArguments
     * @return populated task parameters
     */
    @Override
    public PdfToPngParameters toTaskParameters(PdfToPngTaskCliArguments taskCliArguments) {
        PdfToPngParameters parameters = new PdfToPngParameters(taskCliArguments.getColorType().getEnumValue());
        populateSourceParameters(parameters, taskCliArguments);
        populateAbstractMultipleImageParameters(parameters, taskCliArguments);
        populateOutputPrefix(parameters, taskCliArguments);

        if (taskCliArguments.isPageSelection()) {
            parameters.addAllPageRanges(taskCliArguments.getPageSelection().getPageRangeSet());
        }

        return parameters;
    }

}
