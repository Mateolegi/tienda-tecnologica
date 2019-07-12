package dominio;

import dominio.excepcion.PrestamoException;
import dominio.repositorio.RepositorioLibro;
import dominio.repositorio.RepositorioPrestamo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

public class Bibliotecario {

	public static final String EL_LIBRO_NO_SE_ENCUENTRA_DISPONIBLE = "El libro no se encuentra disponible";
	public static final String LIBROS_PALINDROMOS_NO_SE_PRESTAN = "Los libros palíndromos solo se pueden " +
			"utilizar en la biblioteca";
	private static final int DIAS_PRESTAMO_ISBN = 15;

	private RepositorioLibro repositorioLibro;
	private RepositorioPrestamo repositorioPrestamo;

	public Bibliotecario(RepositorioLibro repositorioLibro, RepositorioPrestamo repositorioPrestamo) {
		this.repositorioLibro = repositorioLibro;
		this.repositorioPrestamo = repositorioPrestamo;

	}

	public void prestar(String isbn, String nombreUsuario) {
		if (esPrestado(isbn)) {
			throw new PrestamoException(EL_LIBRO_NO_SE_ENCUENTRA_DISPONIBLE);
		}
		Libro libro = repositorioLibro.obtenerPorIsbn(isbn);
		if (Objects.nonNull(libro)) {
			if (esPalindromo(Objects.requireNonNull(libro.getTitulo()))) {
				throw new PrestamoException(LIBROS_PALINDROMOS_NO_SE_PRESTAN);
			}
			registrarPrestamo(libro, nombreUsuario);
		}
	}

	private void registrarPrestamo(Libro libro, String nombreUsuario) {
		Date fechaEntrega = calcularFechaEntrega(libro.getIsbn());
		Prestamo prestamo = new Prestamo(new Date(), libro, fechaEntrega, nombreUsuario);
		repositorioPrestamo.agregar(prestamo);
	}

	private Date calcularFechaEntrega(String isbn) {
		if (isbnMayor30Digitos(isbn)) {
			return adicionarDias(new Date(), 15);
		}
		return null;
	}

	private static Date adicionarDias(Date fecha, int dias) {
		LocalDate localDate = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		int count = 1;
		while (count != dias) {
			localDate = localDate.plusDays(1);
			if (localDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
				count++;
			}
		}
		return java.sql.Date.valueOf(localDate);
	}

	public boolean esPrestado(String isbn) {
		return Objects.nonNull(repositorioPrestamo.obtenerLibroPrestadoPorIsbn(isbn));
	}

	private static boolean esPalindromo(String cadena) {
		cadena = cadena.toLowerCase().replace("á", "a")
									 .replace("é", "e")
									 .replace("í", "i")
									 .replace("ó", "o")
									 .replace("ú", "u")
									 .replace(" ", "")
									 .replace(".", "")
									 .replace(",", "");
		String invertida = new StringBuilder(cadena).reverse().toString();
		return invertida.equals(cadena);
	}

	private static boolean isbnMayor30Digitos(String isbn) {
		return isbn.length() > 30;
	}
}
