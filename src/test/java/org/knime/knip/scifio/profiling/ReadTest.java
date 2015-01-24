/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.knime.knip.scifio.profiling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ij.IJ;
import ij.ImagePlus;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.ImgSaver;
import io.scif.img.SCIFIOImgPlus;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.scijava.Context;
import org.scijava.util.FileUtils;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;

@BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 2)
public class ReadTest {

	/** Needed for JUnit-Benchmarks */
	@Rule
	public TestRule benchmarkRun = new BenchmarkRule();

	private final static long[] dims = { 1024, 1024, 2 };

	private final File lonelyDirectory = new File(getClass().getResource("/").getPath(), "../lonely");
	private final File lonelyTestImage = new File(lonelyDirectory, "test.tif");
	private final File clutteredDirectory = new File(getClass().getResource("/").getPath(), "../cluttered");
	private final File testImageInCluttered = new File(clutteredDirectory, "test.tif");
	private Context context = new Context();
	private ImgSaver saver = new ImgSaver(context);
	private ImgOpener opener = new ImgOpener(context);

	@Before
	public void setup() throws IOException, ImgIOException, IncompatibleTypeException {
		// cleanup
		FileUtils.deleteRecursively(lonelyDirectory);
		assertTrue(lonelyDirectory.mkdirs());
		FileUtils.deleteRecursively(clutteredDirectory);
		assertTrue(clutteredDirectory.mkdirs());

		// write the test images
		writeTiff(lonelyTestImage, dims);
		writeTiff(testImageInCluttered, dims);

		// clutter the directory
		final long[] smallDims = new long[] { 256, 256 };
		for (int i = 0; i < 50; i++) {
			writeTiff(new File(clutteredDirectory, "dummy" + i + ".tiff"), smallDims);
		}
	}

	private void writeTiff(final File file, final long[] dims) throws ImgIOException, IncompatibleTypeException {
		final Img<?> img = ArrayImgs.bytes(dims);
		saver.saveImg(file.getPath(), img);
	}

	@Test
	public void testTiffInClutteredDirectory() throws ImgIOException {
		final List<SCIFIOImgPlus<?>> images = opener.openImgs(testImageInCluttered.getPath());
		assertTrue(images.size() == 1);
	}

	@Test
	public void testTiff() throws ImgIOException {
		final List<SCIFIOImgPlus<?>> images = opener.openImgs(lonelyTestImage.getPath());
		assertTrue(images.size() == 1);
	}

	@Test
	public void testTiffImageJ1() throws ImgIOException {
		final ImagePlus imp = IJ.openImage(lonelyTestImage.getPath());
		assertEquals((int) dims[0], imp.getWidth());
		assertEquals((int) dims[1], imp.getHeight());
		assertEquals((int) dims[2], imp.getStackSize());
	}
}
