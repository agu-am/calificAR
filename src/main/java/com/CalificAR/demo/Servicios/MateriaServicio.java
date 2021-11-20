package com.CalificAR.demo.Servicios;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.CalificAR.demo.Entidades.Alumno;
import com.CalificAR.demo.Entidades.Materia;
import com.CalificAR.demo.Entidades.Profesor;
import com.CalificAR.demo.Errores.ErrorServicio;
import com.CalificAR.demo.Repositorio.AlumnoRepositorio;
import com.CalificAR.demo.Repositorio.MateriaRepositorio;
import com.CalificAR.demo.Repositorio.ProfesorRepositorio;

@Service
public class MateriaServicio {

	@Autowired
	private MateriaRepositorio materiaRepositorio;
	@Autowired
	private AlumnoRepositorio alumnoRepositorio;
	@Autowired
	private ProfesorRepositorio profesorRepositorio;

	@Transactional
	public Materia crearMateria(String nombreMateria, String dniProfesor) throws ErrorServicio {
		// validar
		validarMateria(nombreMateria);
		Materia materia = new Materia();
		materia.setNombre(nombreMateria);
		Profesor profesor = profesorRepositorio.buscarPorDni(dniProfesor);
		List<Materia> materias = profesor.getMaterias();
		materias.add(materia);
		profesor.setMaterias(materias);
		profesorRepositorio.save(profesor);
		return materiaRepositorio.save(materia);
	}

	public void validarMateria(String nombreMateria) throws ErrorServicio {
		// Validamos que el nombre de la materia no sea nulo
		if (nombreMateria == null || nombreMateria.isEmpty()) {
			throw new ErrorServicio("El nombre de la materia no debe ser nulo");
		}
		// Validamos que no haya otra materia con el mismo nombre
		// Preguntar si es distinto de nulo y si es así devuelvo excepción.
		Materia respuesta = materiaRepositorio.buscarPorNombre(nombreMateria);
		if (respuesta != null) {
			throw new ErrorServicio("El nombre de la materia ingresada ya existe");
		}
	}

	public void inscribirMateria(String idMateria, String dni) throws ErrorServicio {
		Alumno alumno = alumnoRepositorio.buscarPorDni(dni);
		List<Materia> materias = alumno.getMaterias();
		Optional<Materia> respuesta2 = materiaRepositorio.findById(idMateria);
		if (respuesta2.isPresent()) {
			Materia materia = respuesta2.get();
			if (materias.contains(materia)) {
				throw new ErrorServicio("Ya está incripto en la materia");
			}
			materias.add(materia);
			alumno.setMaterias(materias);
			alumnoRepositorio.save(alumno);
		} else {
			throw new ErrorServicio("No se encontró la materia");
		}
	}

	public List<Materia> todas() {
		return materiaRepositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Materia buscarPorId(String id) throws ErrorServicio {
		Optional<Materia> respuesta = materiaRepositorio.findById(id);
		if (respuesta.isPresent()) {
			Materia materia = respuesta.get();
			return materia;
		} else {
			throw new ErrorServicio("No se encontró la materia solicitada");
		}
	}

//    public List<Materia> materias(Usuario usuario) {
//        List<Materia> materias;
//        if (usuario instanceof Alumno) {
//            materias = alumnoRepositorio.findById(usuario.getId()).get().getMaterias();
//        } else {
//            materias = profesorRepositorio.findById(usuario.getId()).get().getMaterias();
//        }
//        return materias;
//    }
	public List<Materia> materiasPorProfesor(String dni) {
		List<Materia> materias;
		materias = profesorRepositorio.buscarPorDni(dni).getMaterias();
		return materias;
	}

	public List<Materia> materiasPorAlumno(String dni) {
		List<Materia> materias;
		materias = alumnoRepositorio.buscarPorDni(dni).getMaterias();
		return materias;
	}
}
