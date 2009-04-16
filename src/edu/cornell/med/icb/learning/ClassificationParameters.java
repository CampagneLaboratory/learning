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

package edu.cornell.med.icb.learning;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.Set;

/**
 * Abstracts the parameters of a machine learning classifier.
 *
 * @author Fabien Campagne Date: Nov 19, 2007 Time: 9:29:24 AM
 */
public abstract class ClassificationParameters {
    final Set<String> parameterNames;

    public abstract void setParameter(final String parameterName, final double value);

    public Set<String> getExposedParameterNames() {
        return parameterNames;
    }

    protected ClassificationParameters() {
        super();
        this.parameterNames = new ObjectArraySet<String>();
    }

    public void registerExposedParameter(final String parameterName) {
        parameterNames.add(parameterName);
    }
}
