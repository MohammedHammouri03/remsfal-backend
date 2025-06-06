package de.remsfal.service.control;

import de.remsfal.core.json.project.SiteJson;
import de.remsfal.core.model.project.SiteModel;
import de.remsfal.service.entity.dao.SiteRepository;
import de.remsfal.service.entity.dto.SiteEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class SiteController {

    @Inject
    Logger logger;

    @Inject
    SiteRepository repository;

    @Inject
    AddressController addressController;

    @Inject
    TenancyController tenancyController;

    @Transactional
    public SiteModel createSite(final String projectId, final String propertyId, final SiteModel site) {
        logger.infov("Creating a site (projectId={0}, propertyId={1}, site={2})", projectId, propertyId, site);
        SiteEntity entity = new SiteEntity();
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setPropertyId(propertyId);
        entity.setTitle(site.getTitle());
        entity.setDescription(site.getDescription());
        if(site.getAddress() != null) {
            entity.setAddress(addressController.updateAddress(entity.getAddress(), site.getAddress()));
        }
        entity.setUsableSpace(site.getUsableSpace());
        repository.persistAndFlush(entity);
        repository.getEntityManager().refresh(entity);
        return getSite(projectId, entity.getId());
    }

    public SiteModel getSite(final String projectId, final String siteId) {
        logger.infov("Retrieving a site (projectId={0}, siteId={1})",
            projectId, siteId);
        return repository.findSiteById(projectId, siteId)
            .orElseThrow(() -> new NotFoundException("Site not exist"));
    }

    @Transactional
    public SiteModel updateSite(final String projectId,
                                final String siteId, final SiteJson site) {
        logger.infov("Updating a site (projectId={0}, siteId={1}, site={2})",
            projectId, siteId, site);
        final SiteEntity entity = repository.findSiteById(projectId, siteId)
            .orElseThrow(() -> new NotFoundException("Site not exist or user has no membership"));
        if (site.getTitle() != null) {
            entity.setTitle(site.getTitle());
        }
        if (site.getDescription() != null) {
            entity.setDescription(site.getDescription());
        }
        if (site.getAddress() != null) {
            entity.setAddress(addressController.updateAddress(entity.getAddress(), site.getAddress()));
        }
        if (site.getTenancy() != null) {
            entity.setTenancy(tenancyController.updateTenancy(projectId, entity.getTenancy(), site.getTenancy()));
        }
        if (site.getUsableSpace() != null) {
            entity.setUsableSpace(site.getUsableSpace());
        }
        return repository.merge(entity);
    }

    @Transactional
    public boolean deleteSite(final String projectId, final String siteId) {
        logger.infov("Deleting a site (projectId={0}, siteId={1})", projectId, siteId);
        return repository.deleteSiteById(projectId, siteId) > 0;
    }

}
