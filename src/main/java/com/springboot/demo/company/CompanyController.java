package com.springboot.demo.company;

import com.springboot.demo.company.dto.CompanyCreateRequest;
import com.springboot.demo.company.dto.CompanyUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService service;
    public CompanyController(CompanyService service){ this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Company create(@Valid @RequestBody CompanyCreateRequest req){
        return service.create(req);
    }

    @GetMapping("/{id}")
    public Company get(@PathVariable String id){
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Company update(@PathVariable String id,
                          @Valid @RequestBody CompanyUpdateRequest req){
        return service.update(id, req);
    }

    @PatchMapping("/{id}")
    public Company patch(@PathVariable String id, @RequestBody Map<String,Object> p){
        Company c = service.getById(id);
        if (p.containsKey("name")) {
            c.setName(Objects.toString(p.get("name"), "").trim());
        }
        if (p.containsKey("logoUrl")) {
            c.setLogoUrl((String)p.get("logoUrl"));
        }
        return service.save(c);
    }

    @GetMapping
    public Page<Company> list(@RequestParam(required=false) String q,
                              @RequestParam(defaultValue="0") int page,
                              @RequestParam(defaultValue="10") int size){
        return service.search(q, page, size);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id){
        service.delete(id);
    }


}
