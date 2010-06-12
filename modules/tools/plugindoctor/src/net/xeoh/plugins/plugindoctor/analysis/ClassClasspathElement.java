/*
 * ClassAnalysis.java
 * 
 * Copyright (c) 2009, Ralf Biedert All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the author nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package net.xeoh.plugins.plugindoctor.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import net.xeoh.plugins.base.impl.classpath.locator.AbstractClassPathLocation;

import org.gjt.jclasslib.io.ClassFileReader;
import org.gjt.jclasslib.structures.CPInfo;
import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.structures.constants.ConstantClassInfo;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Analyzes a class and stores the result.
 * 
 * @author rb
 *
 */
public class ClassClasspathElement extends AbstractClasspathElement {
    /** */
    final ClassFile classFile;

    /** */
    final ClassReader classReader;

    /** */
    final String className;

    /** */
    final Collection<String> nonSystemDepenencies = new ArrayList<String>();

    /** */
    final Collection<String> annotations = new ArrayList<String>();

    /**
     * @param location 
     * @param name 
     * @throws IOException 
     * @throws InvalidByteCodeException 
     */
    public ClassClasspathElement(AbstractClassPathLocation location, String name)
                                                                                 throws IOException,
                                                                                 InvalidByteCodeException {

        super(location, name);

        InputStream inputStream;

        // Use ASM
        inputStream = location.getInputStream(name);
        this.classReader = new ClassReader(inputStream);
        inputStream.close();

        // Use jClassLib
        inputStream = location.getInputStream(name);
        this.classFile = ClassFileReader.readFromInputStream(inputStream);
        inputStream.close();

        // Get base info
        this.className = this.classReader.getClassName().replaceAll("/", ".");

        // Get complex info
        detectDependencies();
        detectAnnotations();
    }

    /**
     * 
     */
    private void detectAnnotations() {
        this.classReader.accept(new ClassVisitor() {

            public void visitSource(String arg0, String arg1) {
                // TODO Auto-generated method stub

            }

            public void visitOuterClass(String arg0, String arg1, String arg2) {
                // TODO Auto-generated method stub

            }

            public MethodVisitor visitMethod(int arg0, String arg1, String arg2,
                                             String arg3, String[] arg4) {
                // TODO Auto-generated method stub
                return null;
            }

            public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
                // TODO Auto-generated method stub

            }

            public FieldVisitor visitField(int arg0, String arg1, String arg2,
                                           String arg3, Object arg4) {
                // TODO Auto-generated method stub
                return null;
            }

            public void visitEnd() {
                // TODO Auto-generated method stub

            }

            public void visitAttribute(Attribute arg0) {
                // TODO Auto-generated method stub

            }

            public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
                if (arg0 == null) return null;
                ClassClasspathElement.this.annotations.add(arg0.substring(1).replaceAll(";", "").replaceAll("/", "."));

                return null;
            }

            public void visit(int arg0, int arg1, String arg2, String arg3, String arg4,
                              String[] arg5) {
                // TODO Auto-generated method stub

            }
        }, 0);

    }

    /**
     * Load all dependencies
     */
    private void detectDependencies() {

        final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        final CPInfo[] constantPool = this.classFile.getConstantPool();

        for (CPInfo cpInfo : constantPool) {
            if (cpInfo instanceof ConstantClassInfo) {

                final ConstantClassInfo i = (ConstantClassInfo) cpInfo;

                String dependencyName = null;
                boolean isIrrelevant = false;

                // Check found dependency
                try {
                    dependencyName = i.getName().replaceAll("/", ".");

                    if (dependencyName.startsWith("[")) continue;

                    // If this line returns the class was found and is probably irrelevant 
                    // FIXME: Also classes of this application are found! 
                    systemClassLoader.loadClass(dependencyName);

                    isIrrelevant = true;
                } catch (InvalidByteCodeException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    //
                } catch (NoClassDefFoundError e) {
                    //
                }

                // Add dependency
                if (!isIrrelevant && dependencyName != null && !dependencyName.equals(this.className)) {
                    this.nonSystemDepenencies.add(dependencyName);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("ClassElement {\n");
        sb.append("   name = " + this.className + "\n");

        for (String s : this.nonSystemDepenencies) {
            sb.append("   dependency = " + s + "\n");
        }
        for (String s : this.annotations) {
            sb.append("   annotation = " + s + "\n");
        }

        
        sb.append("}");
        return sb.toString();
    }
}
