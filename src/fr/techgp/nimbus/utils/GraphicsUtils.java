package fr.techgp.nimbus.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class GraphicsUtils {

	private static final String JPEG_METADATA_FORMAT_NAME = "javax_imageio_jpeg_image_1.0";
	private static final String MARKER_ROOT_NODE_NAME = "markerSequence";
	private static final String EXIF_MAGIC_NAME = "Exif";
	private static final int ORENTATION_TAG_VAUE = 0x0112;

	private GraphicsUtils() {
		//
	}

	public static enum ExifOrientation {
		TopLeft(1, false, 0),
		TopRight(2, true, 0),
		BottomRight(3, false, 180),
		BottomLeft(4, true, 180),
		LeftTop(5, true, 270),
		RightTop(6, false, 90),
		RightBottom(7, true, 90),
		LeftBottom(8, false, 270);

		private int value;
		private boolean mirrorH;
		private int rotationCW;

		private ExifOrientation(int value, boolean mirror, int rotation) {
			this.value = value;
			this.mirrorH = mirror;
			this.rotationCW = rotation;
		}
		public boolean getMirrorH() {
			return this.mirrorH;
		}
		public int getRotationCW() {
			return this.rotationCW;
		}
		public static ExifOrientation valueOf(int value) {
			for (ExifOrientation orientation : ExifOrientation.values()) {
				if (orientation.value == value)
					return orientation;
			}
			return null;
		}
	}

	/**
	 * J'ai écrit ce code en regardant le comportement de la librairie "Thumbnailator", sous licence MIT.
	 * https://github.com/coobird/thumbnailator/blob/master/src/main/java/net/coobird/thumbnailator/util/exif/ExifUtils.java
	 *
	 * L'idée est de parser les méta-données du fichier JPEG pour rechercher la méta-donnée qui nous intéresse
	 * javax_imageio_jpeg_image_1.0 => markerSequence => Exif => 0x0112 => rotation
	 */
	public static final ExifOrientation getJPEGExifOrientation(ImageReader reader, int imageIndex) throws IOException {
		IIOMetadata metadata = reader.getImageMetadata(imageIndex);
		for (String name : metadata.getMetadataFormatNames()) {
			if (! JPEG_METADATA_FORMAT_NAME.equals(name))
				continue;

			Node root = metadata.getAsTree(name);
			NodeList rootNodes = root.getChildNodes();
			for (int i = 0; i < rootNodes.getLength(); i++) {
				Node rootNode = rootNodes.item(i);
				if (! MARKER_ROOT_NODE_NAME.equals(rootNode.getNodeName()))
					continue;

				NodeList markerNodes = rootNode.getChildNodes();
				for (int j = 0; j < markerNodes.getLength(); j++) {
					IIOMetadataNode metadataNode = (IIOMetadataNode) markerNodes.item(j);
					byte[] bytes = (byte[]) metadataNode.getUserObject();
					if (bytes == null || ! EXIF_MAGIC_NAME.equals(new String(bytes, 0, EXIF_MAGIC_NAME.length())))
						continue;

					// "bytes" structure is :
					// - 0..3 : ExifId
					// - 4 : \0
					// - 5 : padding
					// - 6..13 : tiff header
					// - 14..15 : field count
					// - 16..27 : field 1
					// - 28..39 : field 2 ...
					ByteBuffer buffer = ByteBuffer.wrap(bytes);
					ByteOrder bo = (bytes[6] == 'I' && bytes[7] == 'I') ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
					byte[] temp = new byte[2];
					buffer.position(14);
					buffer.get(temp);
					int fieldCount = ByteBuffer.wrap(temp).order(bo).getShort();
					byte[] field = new byte[12];
					for (int k = 0; k < fieldCount; k++) {
						buffer.get(field);
						// "fields" structure is :
						// - 0..1 : tag as short
						// - 2..3 : type as short
						// - 4..7 : count
						ByteBuffer buffer2 = ByteBuffer.wrap(field).order(bo);
						short tag = buffer2.getShort();
						if (tag != ORENTATION_TAG_VAUE)
							continue;
						short type = buffer2.getShort();
						int count = buffer2.getInt();
						int typeSize = (type == 5) ? 8 : (type == 9 || type == 4) ? 4 : (type == 3) ? 2 : 1;
						int byteSize = count * typeSize;
						int orientation = 0;
						if (byteSize <= 4 && typeSize == 1) {
							for (int l = 0; l < count; l++) {
								orientation = buffer2.get();
							}
						} else if (byteSize <= 4 && typeSize == 2) {
							for (int l = 0; l < count; l++) {
								orientation = buffer2.getShort();
							}
						} else {
							orientation = buffer2.getInt();
						}
						return ExifOrientation.valueOf(orientation);
					}
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Cette méthode pivote l'image "source" de "degrees" degrés dans le sens des aiguilles d'une montre
	 */
	public static final BufferedImage rotateImage(BufferedImage source, int degrees/*0, 90, 180, 270*/) {
		if (degrees == 0)
			return source;
		boolean withAlpha = source.getTransparency() != Transparency.OPAQUE;
		int type = withAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
		int width = (degrees == 180) ? source.getWidth() : source.getHeight();
		int height = (degrees == 180) ? source.getHeight() : source.getWidth();
		int x = (degrees == 270) ? height / 2 : width / 2;
		int y = (degrees == 90) ? width / 2 : height / 2;
		BufferedImage result = new BufferedImage(width, height, type);
		Graphics2D resultGraphics = result.createGraphics();
		resultGraphics.rotate(Math.toRadians(degrees), x, y);
		resultGraphics.drawImage(source, 0, 0, Color.WHITE, null);
		resultGraphics.dispose();
		return result;
	}

	/**
	 * Cette méthode redimensionne l'image "source" aux dimensions données, quitte à déformer l'image.
	 */
	public static final BufferedImage scaleImage(BufferedImage source, int targetWidth, int targetHeight) {
		boolean withAlpha = source.getTransparency() != Transparency.OPAQUE;
		int type = withAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
		BufferedImage result = new BufferedImage(targetWidth, targetHeight, type);
		Graphics2D resultGraphics = result.createGraphics();
		resultGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		resultGraphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
		resultGraphics.dispose();
		return result;
	}


	/**
	 * Cette méthode redimensionne l'image "source" aux dimensions données, quitte à déformer l'image,
	 * mais en essayant de préserver la qualité grâce à un algo trouvé sur Internet (imgscalr-java-image-scaling-library).
	 */
	public static final BufferedImage scaleImage(BufferedImage source, int targetWidth, int targetHeight, boolean superQuality) {
		int currentWidth = source.getWidth();
		int currentHeight = source.getHeight();
		int fraction = superQuality ? 7 : 2;
		BufferedImage image = source;
		do {
			int prevCurrentWidth = currentWidth;
			int prevCurrentHeight = currentHeight;

			if (currentWidth > targetWidth) {
				currentWidth -= (currentWidth / fraction);
				if (currentWidth < targetWidth)
					currentWidth = targetWidth;
			}

			if (currentHeight > targetHeight) {
				currentHeight -= (currentHeight / fraction);
				if (currentHeight < targetHeight)
					currentHeight = targetHeight;
			}

			if (prevCurrentWidth == currentWidth && prevCurrentHeight == currentHeight)
				break;

			// Render the incremental scaled image.
			image = scaleImage(image, currentWidth, currentHeight);
			image.flush();

		} while (currentWidth != targetWidth || currentHeight != targetHeight);

		return image;
	}


	/**
	 * Cette méthode redimensionne l'image "source" afin qu'elle tienne dans le rectangle donnée, en conservant les proportions.
	 */
	public static final BufferedImage scaleImageWithMaxDimensions(BufferedImage source, Integer targetWidth, Integer targetHeight) {
		int width = source.getWidth();
		int height = source.getHeight();
		if (targetWidth != null && width > targetWidth) {
			height = height * targetWidth / width;
			width = targetWidth;
		}
		if (targetHeight != null && height > targetHeight) {
			width = width * targetHeight / height;
			height = targetHeight;
		}
		BufferedImage result = source;
		if (width != source.getWidth() || height != source.getHeight()) {
			result = scaleImage(source, width, height, false);
			result.flush();
		}
		return result;
	}


	/**
	 * Cette méthode crée une miniature du fichier passé en paramètre en conservant les proportions d'origine
	 */
	public static final byte[] scaleImageWithMaxDimensions(File file, Integer targetWidth, Integer targetHeight) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ImageInputStream inputStream = ImageIO.createImageInputStream(file);
				ImageOutputStream outputStream = ImageIO.createImageOutputStream(baos)) {

			Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
			ImageReader reader = readers.next();
			reader.setInput(inputStream);

			ImageWriter writer = ImageIO.getImageWriter(reader);
			writer.setOutput(outputStream);

			BufferedImage image = reader.read(0);
			image = scaleImageWithMaxDimensions(image, targetWidth, targetHeight);
			writer.write(image);
			reader.setInput(null);
			writer.setOutput(null);

		}
		return baos.toByteArray();
	}

}
