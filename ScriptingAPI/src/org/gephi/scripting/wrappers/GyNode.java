/*
Copyright 2008-2012 Gephi
Authors : Luiz Ribeiro <luizribeiro@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2012 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.scripting.wrappers;

import java.awt.Color;
import java.util.Iterator;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.NodeIterator;
import org.gephi.scripting.util.GyNamespace;
import org.python.core.Py;
import org.python.core.PyBoolean;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PySet;
import org.python.core.PyString;
import org.python.core.PyTuple;

/**
 * This class wraps a node from the graph in a way that it is easier to be
 * handled from the scripting language.
 * 
 * <code>GyNode</code> objects are only instantiated by the
 * <code>GyNamespace.getGyEdge</code> method, which is called every time the
 * user tries to access a variable whose name is reserved for nodes on the
 * Gython's namespace.
 * 
 * This class overrides the default implementation of the
 * <code>__findattr_ex__</code> and <code>__setattr__</code> methods from
 * <code>PyObject</code> so that the user can access (read and write) the nodes'
 * attributes in a seamless way.
 * 
 * @author Luiz Ribeiro
 */
public class GyNode extends PyObject {

    /** The namespace in which this object is inserted */
    private GyNamespace namespace;
    /** The edge underlying on this wrapper */
    private Node underlyingNode;
    // Hack to get a few attributes into jythonconsole's auto-completion
    // TODO: get rid of this ugly hack (:
    public int color;
    public float size;
    public String label;
    public int position;
    public float x;
    public float y;
    public boolean fixed;
    public int indegree;
    public int outdegree;
    public int degree;
    public PyList neighbors;

    /**
     * Constructor for the node wrapper.
     * @param namespace     the namespace in which this object is inserted
     * @param node          the node object that will be wrapped
     */
    public GyNode(GyNamespace namespace, Node node) {
        this.namespace = namespace;
        this.underlyingNode = node;
    }

    @Override
    public String toString() {
        return GyNamespace.NODE_PREFIX + underlyingNode.getNodeData().getId();
    }

    /**
     * Retrieves the underlying node object.
     * @return              the underlying node object
     */
    public Node getNode() {
        return underlyingNode;
    }

    @Override
    public void __setattr__(String name, PyObject value) {
        if (name.equals("color")) {
            Color color = (Color) value.__tojava__(Color.class);
            float red = color.getRed() / 255.0f;
            float green = color.getGreen() / 255.0f;
            float blue = color.getBlue() / 255.0f;
            underlyingNode.getNodeData().setColor(red, green, blue);
        } else if (name.equals("size")) {
            float size = (Float) value.__tojava__(Float.class);
            underlyingNode.getNodeData().setSize(size);
        } else if (name.equals("label")) {
            underlyingNode.getNodeData().setLabel(value.toString());
        } else if (name.equals("position")) {
            PyTuple tuple = (PyTuple) value;
            float x = (Float) tuple.__finditem__(0).__tojava__(Float.class);
            float y = (Float) tuple.__finditem__(1).__tojava__(Float.class);
            underlyingNode.getNodeData().setX(x);
            underlyingNode.getNodeData().setY(y);
        } else if (name.equals("x")) {
            float x = (Float) value.__tojava__(Float.class);
            underlyingNode.getNodeData().setX(x);
        } else if (name.equals("y")) {
            float y = (Float) value.__tojava__(Float.class);
            underlyingNode.getNodeData().setY(y);
        } else if (name.equals("fixed")) {
            boolean fixed = (Boolean) value.__tojava__(Boolean.class);
            underlyingNode.getNodeData().setFixed(fixed);
        } else if (name.equals("indegree")) {
            readonlyAttributeError(name);
        } else if (name.equals("outdegree")) {
            readonlyAttributeError(name);
        } else if (name.equals("degree")) {
            readonlyAttributeError(name);
        } else if (name.equals("neighbors")) {
            readonlyAttributeError(name);
        } else if (!name.startsWith("__")) {
            Object obj = null;

            // TODO: support conversions for other object types
            if (value instanceof PyString) {
                obj = (String) value.__tojava__(String.class);
            } else if (value instanceof PyBoolean) {
                obj = (Boolean) value.__tojava__(Boolean.class);
            } else if (value instanceof PyInteger) {
                obj = (Integer) value.__tojava__(Integer.class);
            } else if (value instanceof PyFloat) {
                obj = (Float) value.__tojava__(Float.class);
            }

            if (obj == null) {
                throw Py.AttributeError("Unsupported node attribute type '" + value.getType().getName() + "'");
            }

            underlyingNode.getNodeData().getAttributes().setValue(name, obj);
        } else {
            super.__setattr__(name, value);
        }
    }

    @Override
    public PyObject __findattr_ex__(String name) {
        if (name.equals("color")) {
            int red = (int) Math.round(underlyingNode.getNodeData().r() * 255.0f);
            int green = (int) Math.round(underlyingNode.getNodeData().g() * 255.0f);
            int blue = (int) Math.round(underlyingNode.getNodeData().b() * 255.0f);
            return Py.java2py(new Color(red, green, blue));
        } else if (name.equals("size")) {
            return Py.java2py(new Float(underlyingNode.getNodeData().getSize()));
        } else if (name.equals("label")) {
            return Py.java2py(underlyingNode.getNodeData().getLabel());
        } else if (name.equals("position")) {
            float x = underlyingNode.getNodeData().x();
            float y = underlyingNode.getNodeData().y();
            return new PyTuple(new PyFloat(x), new PyFloat(y));
        } else if (name.equals("x")) {
            return new PyFloat(underlyingNode.getNodeData().x());
        } else if (name.equals("y")) {
            return new PyFloat(underlyingNode.getNodeData().y());
        } else if (name.equals("fixed")) {
            return new PyBoolean(underlyingNode.getNodeData().isFixed());
        } else if (name.equals("indegree")) {
            int indegree = namespace.getGraphModel().getDirectedGraph().getInDegree(underlyingNode);
            return new PyInteger(indegree);
        } else if (name.equals("outdegree")) {
            int outdegree = namespace.getGraphModel().getDirectedGraph().getOutDegree(underlyingNode);
            return new PyInteger(outdegree);
        } else if (name.equals("degree")) {
            int degree = namespace.getGraphModel().getDirectedGraph().getDegree(underlyingNode);
            return new PyInteger(degree);
        } else if (name.equals("neighbors")) {
            NodeIterable nodeIterable = namespace.getGraphModel().getGraph().getNeighbors(underlyingNode);
            PySet nodesSet = new PySet();

            for (NodeIterator nodeItr = nodeIterable.iterator(); nodeItr.hasNext();) {
                GyNode node = namespace.getGyNode(nodeItr.next().getNodeData().getId());
                nodesSet.add(node);
            }

            return nodesSet;
        } else {
            AttributeModel attributeModel = namespace.getWorkspace().getLookup().lookup(AttributeModel.class);
            if (attributeModel.getNodeTable().hasColumn(name)) {
                Object obj = underlyingNode.getNodeData().getAttributes().getValue(name);
                if (obj == null) {
                    return Py.None;
                }
                return Py.java2py(obj);
            }

            return super.__findattr_ex__(name);
        }
    }

    @Override
    public PyObject __rde__(PyObject obj) {
        if (obj instanceof GyNode) {
            PySet edgeSet = new PySet();
            Node target = ((GyNode) obj).getNode();
            Edge edge = namespace.getGraphModel().getMixedGraph().getEdge(underlyingNode, target);

            if (edge != null && edge.isDirected() && edge.getTarget().equals(target)) {
                edgeSet.add(namespace.getGyEdge(edge.getEdgeData().getId()));
            }

            return edgeSet;
        } else if (obj instanceof PySet) {
            PySet edgeSet = new PySet();
            PySet nodeSet = (PySet) obj;

            for (Iterator iter = nodeSet.iterator(); iter.hasNext();) {
                PySet ret = (PySet) this.__rde__((PyObject) iter.next());
                edgeSet.__ior__(ret);
            }

            return edgeSet;
        }

        return null;
    }

    @Override
    public PyObject __lde__(PyObject obj) {
        if (obj instanceof GyNode || obj instanceof PySet) {
            return obj.__rde__(this);
        }

        return null;
    }

    @Override
    public PyObject __bde__(PyObject obj) {
        if (obj instanceof GyNode) {
            PySet edgeSet = new PySet();
            Node target = ((GyNode) obj).getNode();
            Edge edge = namespace.getGraphModel().getMixedGraph().getEdge(underlyingNode, target);

            if (edge != null && !edge.isDirected()) {
                edgeSet.add(namespace.getGyEdge(edge.getEdgeData().getId()));
            } else {
                edge = namespace.getGraphModel().getMixedGraph().getEdge(target, underlyingNode);

                if (edge != null && !edge.isDirected()) {
                    edgeSet.add(namespace.getGyEdge(edge.getEdgeData().getId()));
                }
            }

            return edgeSet;
        } else if (obj instanceof PySet) {
            PySet edgeSet = new PySet();
            PySet nodeSet = (PySet) obj;

            for (Iterator iter = nodeSet.iterator(); iter.hasNext();) {
                PySet ret = (PySet) this.__bde__((PyObject) iter.next());
                edgeSet.__ior__(ret);
            }

            return edgeSet;
        }

        return null;
    }

    @Override
    public PyObject __anye__(PyObject obj) {
        if (obj instanceof GyNode || obj instanceof PySet) {
            PySet edgeSet = new PySet();

            edgeSet.__ior__(this.__lde__(obj));
            edgeSet.__ior__(this.__rde__(obj));
            edgeSet.__ior__(this.__bde__(obj));

            return edgeSet;
        }

        return null;
    }
}
