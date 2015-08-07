/************************************************************
 * Copyright (c) 2015, Lawrence Livermore National Security, LLC.
 * Produced at the Lawrence Livermore National Laboratory.
 * Written by Timothy Meier, meier3@llnl.gov, All rights reserved.
 * LLNL-CODE-673346
 *
 * This file is part of the OpenSM Monitoring Service (OMS) package.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (as published by
 * the Free Software Foundation) version 2.1 dated February 1999.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * OUR NOTICE AND TERMS AND CONDITIONS OF THE GNU GENERAL PUBLIC LICENSE
 *
 * Our Preamble Notice
 *
 * A. This notice is required to be provided under our contract with the U.S.
 * Department of Energy (DOE). This work was produced at the Lawrence Livermore
 * National Laboratory under Contract No.  DE-AC52-07NA27344 with the DOE.
 *
 * B. Neither the United States Government nor Lawrence Livermore National
 * Security, LLC nor any of their employees, makes any warranty, express or
 * implied, or assumes any liability or responsibility for the accuracy,
 * completeness, or usefulness of any information, apparatus, product, or
 * process disclosed, or represents that its use would not infringe privately-
 * owned rights.
 *
 * C. Also, reference herein to any specific commercial products, process, or
 * services by trade name, trademark, manufacturer or otherwise does not
 * necessarily constitute or imply its endorsement, recommendation, or favoring
 * by the United States Government or Lawrence Livermore National Security,
 * LLC. The views and opinions of authors expressed herein do not necessarily
 * state or reflect those of the United States Government or Lawrence Livermore
 * National Security, LLC, and shall not be used for advertising or product
 * endorsement purposes.
 *
 *        file: ClusterVertexShapeTransformer.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.graph.decorators;

import java.awt.Shape;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;

/**
 * a class that will create a vertex shape that is either a polygon or
 * star. The number of sides corresponds to the number of vertices that were
 * collapsed into the vertex represented by this shape.
 * 
 * @param <V>
 * @see  ClusterVertexSizeTransformer
 */

public class ClusterVertexShapeTransformer<V> extends EllipseVertexShapeTransformer<V>
{

     public ClusterVertexShapeTransformer()
    {
       // 3 is absolute minimum, but really too small for most fabrics (over 5K maybe??)
//       setSizeTransformer(new ClusterVertexSizeTransformer<V>(3));
       // 5 should be the normal "small" value (between 2K and 5K)
//       setSizeTransformer(new ClusterVertexSizeTransformer<V>(5));
       // 10 looks good, perhaps the standard default (up to 2K nodes if you have plenty of screen real-estate)
       setSizeTransformer(new ClusterVertexSizeTransformer<V>(10));
       // 20 is fine for medium or small fabrics (300 nodes or less)
//       setSizeTransformer(new ClusterVertexSizeTransformer<V>(20));
    }

    @Override
    public Shape transform(V v)
    {
      if (v instanceof Graph)
      {
        int size = ((Graph) v).getVertexCount();
        if (size < 8)
        {
          int sides = Math.max(size, 3);
          return factory.getRegularPolygon(v, sides);
        }
        else
        {
          return factory.getRegularStar(v, size);
        }
      }
      return super.transform(v);
    }

}
