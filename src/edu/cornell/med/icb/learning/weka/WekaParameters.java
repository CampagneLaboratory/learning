/*
 * Copyright (C) 2007-2009 Institute for Computational Biomedicine,
 *                         Weill Medical College of Cornell University
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.cornell.med.icb.learning.weka;

import edu.cornell.med.icb.learning.ClassificationParameters;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Fabien Campagne Date: Nov 23, 2007 Time: 1:39:18 PM
 */
public class WekaParameters extends ClassificationParameters {
    private final ObjectArrayList<String> options = new ObjectArrayList<String>();

    public String getWekaClassifierClassName() {
        return wekaClassifierClassName;
    }

    private String wekaClassifierClassName;

    @Override
    public void setParameter(final String parameterName, final double value) {
        if (value == value) {
            options.add(parameterName + " " + value);
        } else {
            if (parameterName.startsWith("wekaClass=")) {
                // extract the name of the class that implements the chosen weka classifier.
                wekaClassifierClassName = parameterName.substring("wekaClass=".length());
            } else {
                // value is NaN, parameter is boolean.
                options.add(parameterName);
            }
        }
    }

    public String[] getNative() {
        return options.toArray(new String[options.size()]);
    }
}
