package com.CalificAR.demo.Controladores;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.CalificAR.demo.Entidades.Alumno;
import com.CalificAR.demo.Entidades.Materia;
import com.CalificAR.demo.Entidades.Profesor;
import com.CalificAR.demo.Errores.ErrorServicio;
import com.CalificAR.demo.Servicios.AlumnoServicio;
import com.CalificAR.demo.Servicios.MateriaServicio;

@Controller
@RequestMapping("/materia")
public class MateriaController {

	@Autowired
	AlumnoServicio alumnoServicio;
	@Autowired
	MateriaServicio materiaServicio;

	@PreAuthorize("hasAnyRole('ROLE_USUARIO_REGISTRADO')")
	@GetMapping("/crearMateria")
	public String crearMateria(HttpSession session) {
		Profesor loginUsuario = (Profesor) session.getAttribute("profesorsession");
		System.out.println(loginUsuario);
		if (loginUsuario == null) {
			return "redirect:/index";
		}
		return "crearMateria";
	}

	@PreAuthorize("hasAnyRole('ROLE_USUARIO_REGISTRADO')")
	@PostMapping("/guardarMateria")
	public String guardarMateria(HttpSession session, ModelMap modelo, @RequestParam String nombreMateria)
			throws ErrorServicio {
		Profesor loginUsuario = (Profesor) session.getAttribute("profesorsession");
		if (loginUsuario == null) {
			return "redirect:/index";
		}
		try {
			materiaServicio.crearMateria(nombreMateria, loginUsuario.getLogin().getDni());
		} catch (Exception ex) {
			modelo.put("error", ex.getMessage());
			return "crearMateria.html";
		}
		return "materia";
	}

	@PreAuthorize("hasAnyRole('ROLE_USUARIO_REGISTRADO')")
	@GetMapping("/materia")
	public String materia(HttpSession session, ModelMap modelo, @RequestParam(required = false) String idMateria)
			throws ErrorServicio {
		Alumno loginAlumno = (Alumno) session.getAttribute("alumnosession");
		Profesor loginProfesor = (Profesor) session.getAttribute("profesorsession");
		if (loginAlumno == null && loginProfesor == null) {
			return "redirect:/index";
		}
		Materia materia = materiaServicio.buscarPorId(idMateria);
		modelo.put("materia", materia);
		return "Materia";
	}

	@PreAuthorize("hasAnyRole('ROLE_USUARIO_REGISTRADO')")
	@GetMapping("/misMaterias")
	public String listarMaterias(HttpSession session, ModelMap modelo) {
		Alumno loginAlumno = (Alumno) session.getAttribute("alumnosession");
		Profesor loginProfesor = (Profesor) session.getAttribute("profesorsession");
		if (loginAlumno == null && loginProfesor == null) {
			return "redirect:/index";
		}
		List<Materia> materias = new ArrayList<>();
		// Es un alumno
		if (loginAlumno != null) {
			materias = materiaServicio.materiasPorAlumno(loginAlumno.getLogin().getDni());
		}
		// Es un profesor
		if (loginProfesor != null) {
			materias = materiaServicio.materiasPorProfesor(loginProfesor.getLogin().getDni());// ACA TOQUÉ
		}
		modelo.put("materias", materias);
		return "misMaterias";
	}

	@PreAuthorize("hasAnyRole('ROLE_USUARIO_REGISTRADO')")
	@GetMapping("/inscribirseMaterias")
	public String inscribirseMaterias(HttpSession session, ModelMap modelo, @RequestParam String idMateria)
			throws ErrorServicio {
		Alumno loginUsuario = (Alumno) session.getAttribute("alumnosession");
		if (loginUsuario == null) {
			return "redirect:/index";
		}
		try {
			materiaServicio.inscribirMateria(idMateria, loginUsuario.getLogin().getDni());
		} catch (ErrorServicio e) {
			modelo.put("error", e.getMessage()); // <p th:if="${error != null}" th:text="${error}"
													// style="color:red;"></p>
			List<Materia> materias = alumnoServicio.buscarMateriasParaInscribirse(loginUsuario.getId());
			modelo.put("materias", materias);
			return "inicio";
		}
		modelo.put("mensaje", "Inscripto correctamente!");
		Materia materia = materiaServicio.buscarPorId(idMateria);
		modelo.put("materia", materia);
		return "Materia";
	}
}
