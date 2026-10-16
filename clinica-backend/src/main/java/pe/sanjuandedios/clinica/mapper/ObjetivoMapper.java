package pe.sanjuandedios.clinica.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.sanjuandedios.clinica.domain.BancoObjetivo;
import pe.sanjuandedios.clinica.domain.ObjetivoCiclo;
import pe.sanjuandedios.clinica.dto.BancoObjetivoDTO;
import pe.sanjuandedios.clinica.dto.ObjetivoActivoDTO;

@Mapper
public interface ObjetivoMapper {

    @Mapping(target = "bankId", source = "bancoObjetivo.id")
    @Mapping(target = "text", source = "bancoObjetivo.texto")
    @Mapping(target = "baseline", source = "lineaBase")
    @Mapping(target = "target", source = "meta")
    @Mapping(target = "addedInSession", source = "sesionInicio")
    @Mapping(target = "order", source = "orden")
    ObjetivoActivoDTO toDto(ObjetivoCiclo objetivo);

    List<ObjetivoActivoDTO> toDtoList(List<ObjetivoCiclo> objetivos);

    @Mapping(target = "specialtyId", source = "especialidad.id")
    @Mapping(target = "text", source = "texto")
    BancoObjetivoDTO toDto(BancoObjetivo bancoObjetivo);

    List<BancoObjetivoDTO> toBancoDtoList(List<BancoObjetivo> bancoObjetivos);
}
