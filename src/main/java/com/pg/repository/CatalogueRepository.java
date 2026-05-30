package com.pg.repository;
import com.pg.model.Catalogue;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
public interface CatalogueRepository extends MongoRepository<Catalogue, String> {
    List<Catalogue> findByType(String type);
}
