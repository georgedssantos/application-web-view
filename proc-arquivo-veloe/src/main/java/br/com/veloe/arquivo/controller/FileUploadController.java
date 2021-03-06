package br.com.veloe.arquivo.controller;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.veloe.arquivo.model.entity.FileTRN;
import br.com.veloe.arquivo.model.enums.PassageStatus;
import br.com.veloe.arquivo.model.enums.TypeCategory;
import br.com.veloe.arquivo.service.FileService;

@Controller
@RequestMapping("/files-TRN")
public class FileUploadController {
	
	private static final String PAGE_FILE_TRN = "pages/files-trn";
	private static final String PAGE_FILE_VIEW_TRN = "pages/file-view-trn";

	@Autowired
	private FileService fileService;
	
	@Autowired
	private BuildProperties buildProperties;
	
		
	@RequestMapping 
    public String search(HttpServletRequest request, Model model) {
		
        int page = 0; //DEFAULT PAGE NUMBER IS 0 (YES IT IS WEIRD)
        int size = 3; //PAGE SIZE
        
        if (request.getParameter("page") != null && !request.getParameter("page").isEmpty()) {
            page = Integer.parseInt(request.getParameter("page")) - 1;
        }

        if (request.getParameter("size") != null && !request.getParameter("size").isEmpty()) {
            size = Integer.parseInt(request.getParameter("size"));
        }
		
		model.addAttribute("files", this.fileService.searchPage(page, size));
        
        return PAGE_FILE_TRN;
    }
	
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes attributes) {
    	    	    	
    	String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        // check if file is empty
        if (file.isEmpty()) {
            attributes.addFlashAttribute("messageError", "Selecione um arquivo para enviar.");
            return "redirect:/files-TRN";
        }
        
        // check if file is TRN
        if(!org.springframework.util.StringUtils.getFilenameExtension(file.getOriginalFilename()).equalsIgnoreCase("TRN")) {
            attributes.addFlashAttribute("messageError", "Extens??o inv??lida para o arquivo " + fileName + ". Somente arquivo TRN ?? suportado.");
            return "redirect:/files-TRN";
        }

		try {
			Path path = this.fileService.upload(file);
			FileTRN fileTRN = this.fileService.readFileTRN(path);
			this.fileService.save(fileTRN);
			attributes.addFlashAttribute("message", "Upload feito com sucesso! " + fileName + '!');
		} catch (FileSizeLimitExceededException | MaxUploadSizeExceededException e) {
			attributes.addFlashAttribute("messageError", "O tamanho do arquivo excede o limite!");
		} catch (Exception e) {
			attributes.addFlashAttribute("messageError", "Erro: " + e.getMessage() + '!');
		}
        
        return "redirect:/files-TRN";
    }
    
	@RequestMapping("{code}")
	public ModelAndView view(@PathVariable("code") Long idFile) {	
		ModelAndView modelAndView = new ModelAndView(PAGE_FILE_VIEW_TRN);
		try {			
			FileTRN file = this.fileService.getFile(idFile);
			modelAndView.addObject("file", file);			
		} catch (Exception e) {
			modelAndView.addObject("messageError", "Erro: " + e.getMessage());
		}
		return modelAndView;
	}
	
	@ModelAttribute("listPassageStatus")
	public List<PassageStatus> listPassageStatus() {
		return Arrays.asList(PassageStatus.values());
	}
	
	@ModelAttribute("listTypeCategory")
	public List<TypeCategory> listTypeCategory() {
		return Arrays.asList(TypeCategory.values());
	}
  
	@ModelAttribute("version")
	public String version() {
		return buildProperties.getVersion();
	}

}