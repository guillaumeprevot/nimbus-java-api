package fr.techgp.nimbus.server.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.StandardCopyOption;

import jakarta.servlet.http.Part;

import org.eclipse.jetty.server.MultiPartFormInputStream;

public class JettyOptimizedUpload extends ServletUpload {

	public JettyOptimizedUpload(Part part) {
		super(part);
	}

	@Override
	public void saveTo(File storedFile) throws IOException {
		if (this.raw() instanceof MultiPartFormInputStream.MultiPart) {
			MultiPartFormInputStream.MultiPart mpart = (MultiPartFormInputStream.MultiPart) this.raw();
			if (mpart.getFile() != null) {
				// La limite a été dépassée et le fichier a donc été écrit sur disque.
				// => on déplace le fichier (= rapide puisque c'est le même volume)
				java.nio.file.Files.move(mpart.getFile().toPath(), storedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				return;
			}
			if (mpart.getBytes() != null) {
				// La taille est en dessous de la limite et le contenu est donc en mémoire
				// => on écrit dans le fichier demandé sans avoir besoin de buffer supplémentaire
				try (OutputStream os = new FileOutputStream(storedFile)) {
					os.write(mpart.getBytes());
				}
				return;
			}
		}
		// Sinon, on utilise la méthode par défaut qui fait de la copie de flux
		// => c'est le cas par défaut, si on n'a détecté ni fichier temporaire, ni byte[] en mémoire
		super.saveTo(storedFile);
	}

	@Override
	public void saveTo(OutputStream storedStream) throws IOException {
		if (this.raw() instanceof MultiPartFormInputStream.MultiPart) {
			MultiPartFormInputStream.MultiPart mpart = (MultiPartFormInputStream.MultiPart) this.raw();
			if (mpart.getBytes() != null) {
				// La taille est en dessous de la limite et le contenu est donc en mémoire
				// => on écrit dans le flux demandé sans avoir besoin de buffer supplémentaire
				storedStream.write(mpart.getBytes());
				return;
			}
		}
		// Sinon, on utilise la méthode par défaut qui fait de la copie de flux
		// => c'est le cas par défaut ou le cas où l'upload a généré un fichier temporaire
		super.saveTo(storedStream);
	}

}
