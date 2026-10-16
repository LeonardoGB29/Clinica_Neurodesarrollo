package pe.sanjuandedios.clinica.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.sanjuandedios.clinica.domain.Ciclo;
import pe.sanjuandedios.clinica.domain.EvaluacionObjetivo;
import pe.sanjuandedios.clinica.domain.ObjetivoCiclo;
import pe.sanjuandedios.clinica.domain.Paciente;
import pe.sanjuandedios.clinica.domain.Profesional;
import pe.sanjuandedios.clinica.domain.Sesion;
import pe.sanjuandedios.clinica.domain.progreso.NivelGas;
import pe.sanjuandedios.clinica.domain.progreso.ProgresoService;
import pe.sanjuandedios.clinica.domain.progreso.SesionRegistrada;
import pe.sanjuandedios.clinica.repository.CicloRepository;
import pe.sanjuandedios.clinica.repository.SesionRepository;
import pe.sanjuandedios.clinica.web.error.RecursoNoEncontradoException;

/**
 * Reporte PDF de la ficha de evolución (OpenPDF): cabecera del paciente y del profesional,
 * resumen de objetivos con % acumulado / última sesión / GAS y el detalle de cada sesión registrada.
 */
@Service
@RequiredArgsConstructor
public class ReportePdfService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.of("es", "PE"));
    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font ETIQUETA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font PIE = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8);

    private final CicloRepository cicloRepository;
    private final SesionRepository sesionRepository;
    private final ProgresoService progresoService;

    @Transactional(readOnly = true)
    public byte[] generar(String cicloId) {
        Ciclo ciclo = cicloRepository.findDetalladoById(cicloId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ciclo", cicloId));
        List<Sesion> sesiones = sesionRepository.findByCicloIdOrderByNumeroAsc(cicloId);
        List<SesionRegistrada> dominio = SesionesDominio.aDominio(sesiones);
        List<ObjetivoCiclo> objetivos = ciclo.getObjetivosActivos();

        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(documento, salida);
            documento.addTitle("Ficha de evolución " + ciclo.getId());
            documento.addAuthor("Clínica San Juan de Dios – Centro de Neurodesarrollo");
            documento.open();

            documento.add(new Paragraph("Reporte de Ficha de Evolución", TITULO));
            documento.add(new Paragraph("Centro de Neurodesarrollo (TEA/TDAH) · Clínica San Juan de Dios", TEXTO));
            documento.add(new Paragraph("Generado el " + formatear(LocalDate.now()), PIE));
            documento.add(espacio());

            documento.add(cabecera(ciclo));
            documento.add(espacio());

            documento.add(new Paragraph("Objetivos del ciclo y progreso", SUBTITULO));
            documento.add(tablaObjetivos(objetivos, dominio));
            documento.add(espacio());

            documento.add(new Paragraph("Sesiones registradas", SUBTITULO));
            if (sesiones.isEmpty()) {
                documento.add(new Paragraph("Aún no hay sesiones registradas en este ciclo.", TEXTO));
            }
            for (Sesion sesion : sesiones) {
                documento.add(bloqueSesion(sesion, objetivos));
            }

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new IllegalStateException("No fue posible generar el reporte PDF del ciclo " + cicloId, e);
        }
    }

    // ------------------------------------------------------------------ secciones

    private PdfPTable cabecera(Ciclo ciclo) {
        Paciente paciente = ciclo.getPaciente();
        Profesional profesional = ciclo.getProfesional();
        int edad = Period.between(paciente.getFechaNac(), LocalDate.now()).getYears();

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.addCell(celdaTitulo("Paciente"));
        tabla.addCell(celdaTitulo("Profesional a cargo"));

        Phrase datosPaciente = new Phrase();
        datosPaciente.add(new Phrase(paciente.getNombres() + "\n", ETIQUETA));
        datosPaciente.add(new Phrase(paciente.getNroHistoria() + " · " + edad + " años · "
                + ("M".equals(paciente.getSexo()) ? "Masculino" : "Femenino")
                + " · Nac. " + formatear(paciente.getFechaNac()) + "\n", TEXTO));
        datosPaciente.add(new Phrase("Diagnósticos: " + String.join("; ", paciente.getDiagnosticos()) + "\n", TEXTO));
        if (paciente.getApoderadoNombre() != null) {
            datosPaciente.add(new Phrase("Apoderado: " + paciente.getApoderadoNombre()
                    + " (" + paciente.getApoderadoRelacion() + ") · " + paciente.getApoderadoTelefono() + "\n", TEXTO));
        }
        if (paciente.getCobertura() != null) {
            datosPaciente.add(new Phrase("Cobertura: " + paciente.getCobertura() + "\n", TEXTO));
        }
        tabla.addCell(celda(datosPaciente));

        Phrase datosProfesional = new Phrase();
        datosProfesional.add(new Phrase(profesional.getNombres() + "\n", ETIQUETA));
        datosProfesional.add(new Phrase(ciclo.getEspecialidad().getNombre() + " · " + profesional.getLicencia() + "\n\n", TEXTO));
        datosProfesional.add(new Phrase("Ciclo: " + ciclo.getId() + "\n", TEXTO));
        datosProfesional.add(new Phrase("Inicio " + formatear(ciclo.getFechaInicio())
                + (ciclo.getFrecuencia() != null ? " · " + ciclo.getFrecuencia() : "") + "\n", TEXTO));
        datosProfesional.add(new Phrase(ciclo.estaAbierto()
                ? "Sesión " + ciclo.getSesionActual() + " de " + ciclo.getTotalSesiones() + " · En curso"
                : "Ciclo cerrado el " + formatear(ciclo.getFechaCierre()) + " · "
                        + ciclo.getTotalSesiones() + " sesiones", TEXTO));
        tabla.addCell(celda(datosProfesional));
        return tabla;
    }

    private PdfPTable tablaObjetivos(List<ObjetivoCiclo> objetivos, List<SesionRegistrada> sesiones) throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[] {4f, 1f, 1f, 1.2f, 1.2f, 2.2f});
        tabla.setWidthPercentage(100);
        for (String encabezado : List.of("Objetivo", "Línea base", "Meta", "Acumulado", "Última sesión", "GAS (en vivo)")) {
            tabla.addCell(celdaTitulo(encabezado));
        }
        if (objetivos.isEmpty()) {
            PdfPCell vacia = celda(new Phrase("Sin objetivos activos.", TEXTO));
            vacia.setColspan(6);
            tabla.addCell(vacia);
        }
        for (ObjetivoCiclo objetivo : objetivos) {
            Integer ultimo = progresoService.ultimoPuntaje(sesiones, objetivo.getId());
            NivelGas gas = progresoService.calcularGas(ultimo, objetivo.getLineaBase(), objetivo.getMeta());
            tabla.addCell(celda(new Phrase(objetivo.getBancoObjetivo().getTexto(), TEXTO)));
            tabla.addCell(celdaCentrada(objetivo.getLineaBase() + " %"));
            tabla.addCell(celdaCentrada(objetivo.getMeta() + " %"));
            tabla.addCell(celdaCentrada(progresoService.progresoAcumulado(sesiones, objetivo.getId()) + " %"));
            tabla.addCell(celdaCentrada(ultimo == null ? "—" : ultimo + " %"));
            tabla.addCell(celda(new Phrase(gas == null ? "Sin evaluar"
                    : formatearNivel(gas.getNivel()) + " · " + gas.getEtiqueta() + " · " + gas.getDecision(), TEXTO)));
        }
        return tabla;
    }

    private Paragraph bloqueSesion(Sesion sesion, List<ObjetivoCiclo> objetivos) {
        Paragraph bloque = new Paragraph();
        bloque.setSpacingBefore(8);
        bloque.add(new Phrase("Sesión " + sesion.getNumero() + " · " + formatear(sesion.getFecha())
                + " · " + sesion.getProfesional().getNombres() + "\n", ETIQUETA));

        StringBuilder evaluaciones = new StringBuilder();
        for (EvaluacionObjetivo evaluacion : sesion.getEvaluaciones()) {
            Integer tasa = progresoService.tasaExito(evaluacion.getAciertos(), evaluacion.getIntentos());
            evaluaciones.append("  • ").append(evaluacion.getObjetivoCiclo().getBancoObjetivo().getTexto())
                    .append(": ").append(evaluacion.getAciertos()).append(" de ").append(evaluacion.getIntentos())
                    .append(" (").append(tasa == null ? "—" : tasa + " %").append(")\n");
        }
        if (!evaluaciones.isEmpty()) {
            bloque.add(new Phrase("Evaluación de objetivos:\n" + evaluaciones, TEXTO));
        }
        bloque.add(new Phrase("Actividad: ", ETIQUETA));
        bloque.add(new Phrase(sesion.getActividad() + "\n", TEXTO));
        bloque.add(new Phrase("Logro: ", ETIQUETA));
        bloque.add(new Phrase(sesion.getLogro() + "\n", TEXTO));
        if (sesion.getObservaciones() != null) {
            bloque.add(new Phrase("Observaciones / recomendaciones: ", ETIQUETA));
            bloque.add(new Phrase(sesion.getObservaciones() + "\n", TEXTO));
        }
        return bloque;
    }

    // ------------------------------------------------------------------ utilidades

    private static PdfPCell celdaTitulo(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, ETIQUETA));
        celda.setPadding(5);
        celda.setGrayFill(0.9f);
        return celda;
    }

    private static PdfPCell celda(Phrase contenido) {
        PdfPCell celda = new PdfPCell(contenido);
        celda.setPadding(5);
        return celda;
    }

    private static PdfPCell celdaCentrada(String texto) {
        PdfPCell celda = celda(new Phrase(texto, TEXTO));
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        return celda;
    }

    private static Paragraph espacio() {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(4);
        return p;
    }

    private static String formatear(LocalDate fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }

    private static String formatearNivel(int nivel) {
        return nivel > 0 ? "+" + nivel : String.valueOf(nivel);
    }
}
