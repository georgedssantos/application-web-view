package br.com.veloe.arquivo.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import br.com.veloe.arquivo.model.entity.FileDetailTRN;
import br.com.veloe.arquivo.model.entity.FileTRN;
import br.com.veloe.arquivo.model.enums.PassageStatus;
import br.com.veloe.arquivo.model.enums.TypeCategory;
import br.com.veloe.arquivo.repository.FileDetailTRNRepository;
import br.com.veloe.arquivo.repository.FileTRNRepository;

@Service
public class FileServiceImpl implements FileService {
	
	@Autowired
	private FileTRNRepository fileTRNRepository;
	
	@Autowired
	private FileDetailTRNRepository fileDetailTRNRepository;
	
    @Value("${file.path}")
    private String filePath;
	
	@Override
	public Path upload(MultipartFile file) throws FileSizeLimitExceededException, MaxUploadSizeExceededException, IOException {	
		//String filePath = new ClassPathResource("/src/main/resources/files/").getPath();
		File dirPath = new File(filePath);
        if (!dirPath.exists()) {
        	dirPath.mkdirs();
        }
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Path path = Paths.get(filePath + fileName);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        return path;
	}

	@Override
	public FileTRN readFileTRN(Path path) throws Exception {
        List<String> rows = Files.readAllLines(path);
        
        List<FileDetailTRN> details = new ArrayList<>();
        FileTRN file = new FileTRN();
        
        for (String row : rows) {
        	
        	//CABECALHO
        	if(row == rows.get(0)) {
        		//System.out.println("linha: "+row.substring(0));
        		
            	String tipo = row.substring(0,2);
                //System.out.println("Tipo: "+tipo);
                file.setTipo(tipo);
                
            	//String pais = row.substring(2,6);
                //System.out.println("Pais: "+pais);
                
                String idConcessionaria = row.substring(6,11);
                //System.out.println("idConcessionaria: "+idConcessionaria);
                file.setIdConcessionaria(idConcessionaria);
                
                String sequencial = row.substring(11,16);
                //System.out.println("Sequencial: "+sequencial);
                file.setSequencial(sequencial);
                
                String dataGeracao = row.substring(16,24);
                String dataGeracaoFormatada = dateFormat(dataGeracao);
                //System.out.println("DataGeracao: "+dataGeracaoFormatada);
                file.setDataGeracao(dataGeracaoFormatada);
                
                String horaGeracao = row.substring(24,30);
                String horaGeracaoFormatada = timeFormat(horaGeracao);
                //System.out.println("HoraGeracao: "+horaGeracaoFormatada);
                file.setHoraGeracao(horaGeracaoFormatada);
                
                String totalRegistro = row.substring(30,36);
                //System.out.println("TotalRegistro: "+totalRegistro);
                file.setTotalRegistro(totalRegistro);
                
                String valorTotal = row.substring(36,48);
                //System.out.println("valorTotal: "+valorTotal);
                valorTotal = valorTotal.replaceAll("^0*", "");            
                valorTotal = valorTotal.substring(0, valorTotal.length()-2) + "," + valorTotal.substring(valorTotal.length()-2);
                //System.out.println("valorTotal: "+valorTotal);
                file.setValorTotal(valorTotal);
                
                continue;
                
        	}else {
        		//DETALHES
        		
        		FileDetailTRN fileDetailTRN = new FileDetailTRN();
        		
        		//System.out.println("linha: "+row.substring(0));
        		//System.out.println("Tipo: "+row.substring(0,1));
        		
        		String seqRegistro = row.substring(1,7);
        		//System.out.println("SeqRegistro: "+seqRegistro);
        		fileDetailTRN.setSeqRegistro(seqRegistro);
        		
        		//String paisEmissor = row.substring(7,11);
        		//System.out.println("paisEmissor: "+paisEmissor);
        		
        		String emissorTag = row.substring(11,16);
        		//System.out.println("emissorTag: "+emissorTag);
        		fileDetailTRN.setEmissorTag(emissorTag);
        		
        		String numeroTag = row.substring(16,26);
        		//System.out.println("numeroTag: "+numeroTag);
        		fileDetailTRN.setNumeroTag(numeroTag);
        		
        		String data = row.substring(26,34);
        		String dataFormatada = dateFormat(data);
        		//System.out.println("data: "+dataFormatada);
        		fileDetailTRN.setData(dataFormatada);
        		
        		String hora = row.substring(34,40);
        		String horaFormatada = timeFormat(hora);
        		//System.out.println("hora: "+horaFormatada);
        		fileDetailTRN.setHora(horaFormatada);
        		
        		String praca = row.substring(40,44);
        		//System.out.println("praca: "+praca);
        		fileDetailTRN.setPraca(praca);
        		
        		String pista = row.substring(44,47);
        		//System.out.println("pista: "+pista);
        		fileDetailTRN.setPista(pista);
        		
        		String categoriaTag = row.substring(47,49);
        		//System.out.println("categoriaTag: "+categoriaTag);
        		fileDetailTRN.setCategoriaTag(categoriaTag);
        		
        		String categoriaDetectada = row.substring(49,51);
        		//System.out.println("categoriaDetectada: "+categoriaDetectada);
        		fileDetailTRN.setCategoriaDetectada(categoriaDetectada);
        		
        		String categoriaCobrada = row.substring(51,53);
        		//System.out.println("categoriaCobrada: "+categoriaCobrada);
        		TypeCategory typeCategory = TypeCategory.getByCodigo(categoriaCobrada);
        		//System.out.println("typeCategory: "+typeCategory.getDescription());
        		fileDetailTRN.setCategoriaCobrada(typeCategory);
        		
                String valorPassagem = row.substring(53,61);
                //System.out.println("valorPassagem: "+valorPassagem);
                valorPassagem = valorPassagem.replaceAll("^0*", "");            
                valorPassagem = valorPassagem.substring(0, valorPassagem.length()-2) + "," + valorPassagem.substring(valorPassagem.length()-2);
                //System.out.println("valorPassagem: "+valorPassagem);
                fileDetailTRN.setValorPassagem(valorPassagem);
                
          		String statusCobranca = row.substring(61,62);
          		statusCobranca = statusCobranca.equalsIgnoreCase("0") ? "Normal" : "Corrigida";
        		//System.out.println("statusCobran??a: "+ statusCobranca);
        		fileDetailTRN.setStatusCobranca(statusCobranca);
        		
          		String statusPassagem = row.substring(62,63);
          		//System.out.println("statusPassagem: "+statusPassagem);
          		PassageStatus passageStatusEnum = PassageStatus.getByCodigo(Integer.valueOf(statusPassagem));
        		//System.out.println("statusPassagem: "+passageStatusEnum.getDescription());
        		fileDetailTRN.setStatusPassagem(passageStatusEnum);
        		
          		String flagBateria = row.substring(63,64);
          		flagBateria = flagBateria.equalsIgnoreCase("0") ? "Ok" : "Bateria Baixa";
        		//System.out.println("flagBateria: "+ flagBateria);
        		fileDetailTRN.setFlagBateria(flagBateria);
        		
          		String flagViolacao = row.substring(64,65);
          		flagViolacao = flagViolacao.equalsIgnoreCase("0") ? "0-N??o Violada" : "1-Violada";
        		//System.out.println("flagViolacao: "+ flagViolacao);
        		fileDetailTRN.setFlagViolacao(flagViolacao);
        		
          		String transacao = row.substring(65,72);
        		//System.out.println("transacao: "+transacao);
        		fileDetailTRN.setTransacao(transacao);
        		
          		String placa = row.substring(72,79);
        		//System.out.println("placa: "+placa);
        		fileDetailTRN.setPlaca(placa);
        		
        		//System.out.println("Pais: "+row.substring(79,83));
        		
          		//String codConcessionariaAntena = row.substring(83,88);
        		//System.out.println("codConcessionariaAntena: "+codConcessionariaAntena);
        		
        		details.add(fileDetailTRN);
        	}
 
        }
        
        file.setFileDetails(details);
        return file;
	}
	
	@Override
	public void save(FileTRN fileTRN) {
		Optional<FileTRN> fileExistence = this.fileTRNRepository.findBySequencial(fileTRN.getSequencial());
		if(!fileExistence.isPresent()) {
			fileTRN.setId(null);
			this.fileTRNRepository.save(fileTRN);
			saveFileDetail(fileTRN.getId(), fileTRN.getFileDetails());
		}
	}
	
	private void saveFileDetail(Long idFile, List<FileDetailTRN> details) {
		for (FileDetailTRN detail : details) {
			detail.setIdFile(idFile);
			this.fileDetailTRNRepository.save(detail);
		}
	}
	
	@Override
	public Page<FileTRN> searchPage(Integer page, Integer size) {
		Page<FileTRN> files = this.fileTRNRepository.findAll(PageRequest.of(page, size));	
		return files;		
	}
	
	@Override
	public List<FileTRN> list() {
		return this.fileTRNRepository.findAll();
	}
	
	@Override
	public FileTRN getFile(Long idFile) throws Exception {
		Optional<FileTRN> fileFound = this.fileTRNRepository.findById(idFile);
		FileTRN file = fileFound.orElseThrow(() -> new Exception("Arquivo com id "+idFile+" n??o foi encontrado."));
		List<FileDetailTRN> fileDetails = this.fileDetailTRNRepository.findByIdFile(idFile);
		file.setFileDetails(fileDetails);
		return file;
	}

	public String dateFormat(String date) throws Exception {
		LocalDate dateTime = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));		
		return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}
	
	public String timeFormat(String time) throws Exception {
		LocalTime localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HHmmss"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
		return formatter.format(localTime);
	}

}
