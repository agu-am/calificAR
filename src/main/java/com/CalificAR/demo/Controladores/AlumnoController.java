package com.CalificAR.demo.Controladores;

import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.CalificAR.demo.Entidades.Alumno;
import com.CalificAR.demo.Entidades.Materia;
import com.CalificAR.demo.Errores.ErrorServicio;
import com.CalificAR.demo.Servicios.AlumnoServicio;
import com.CalificAR.demo.Servicios.NotificacionServicio;

@Controller
@RequestMapping("/alumno")
public class AlumnoController {

	@Autowired
	AlumnoServicio alumnoServicio;
	@Autowired
	NotificacionServicio notificacionServicio;

	@GetMapping("/registroAlumno")
	public String registro(ModelMap modelo) {
		modelo.addAttribute("alumno", new Alumno());
		return "registroAlumno";
	}

	@PostMapping("/crearAlumno")
	public String newAlumno(ModelMap modelo, @ModelAttribute Alumno alumno, String dni, String clave, String clave2,
			MultipartFile archivo) throws ErrorServicio {
		try {
			alumno = alumnoServicio.registrar(archivo, dni, alumno.getNombre(), alumno.getApellido(), alumno.getMail(),
					clave, clave2, alumno.getFechaNac());
			notificacionServicio.enviarBienvenidaAlumno(alumno, dni);
		} catch (ErrorServicio ex) {
			modelo.put("error", ex.getMessage());
			modelo.put("nombre", alumno.getNombre());
			modelo.put("apellido", alumno.getApellido());
			modelo.put("dni", dni);
			modelo.put("mail", alumno.getMail());
			modelo.put("fechaNac", alumno.getFechaNac());
			return "registroAlumno.html";
		}
		modelo.put("titulo", "Bienvenido a CalificAR");
		modelo.put("descripcion", "Su usuario fue registrado de manera satisfactoria");
		modelo.put("alumno", alumno);
		List<Materia> materias = alumnoServicio.buscarMateriasParaInscribirse(alumno.getId());
		modelo.put("materias", materias);
		return "inicio.html";
	}

	@PreAuthorize("hasAnyRole('ROLE_ALUMNO_REGISTRADO')")
	@PostMapping("/guardarAlumno")
	public String modificarAlumno(HttpSession session, ModelMap modelo, @ModelAttribute Alumno alumno,
			@RequestParam String claveAnterior, @RequestParam String claveNueva, MultipartFile archivo)
			throws ErrorServicio {
		Alumno loginAlumno = (Alumno) session.getAttribute("alumnosession");
		if (loginAlumno == null) {
			return "redirect:/index";
		}
		try {
			alumno = alumnoServicio.modificar(archivo, loginAlumno.getLogin().getDni(), alumno.getNombre(),
					alumno.getApellido(), alumno.getMail(), claveNueva, alumno.getFechaNac(), claveAnterior);
		} catch (ErrorServicio ex) {
			modelo.put("error", ex.getMessage());
			modelo.put("nombre", alumno.getNombre());
			modelo.put("apellido", alumno.getApellido());
			modelo.put("mail", alumno.getMail());
			modelo.put("fechaNac", alumno.getFechaNac());
			return "modificarUsuario.html";
		}
		modelo.addAttribute("alumno", alumno);
		session.setAttribute("alumnosession", alumno);
		return "inicio";
	}
}
