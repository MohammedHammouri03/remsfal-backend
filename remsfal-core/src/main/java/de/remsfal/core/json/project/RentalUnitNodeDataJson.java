package de.remsfal.core.json.project;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.project.RentalUnitModel;
import de.remsfal.core.model.project.TenancyModel;
import jakarta.annotation.Nullable;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value.Immutable
@Schema(description = "Encapsulated data of a project tree node")
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public interface RentalUnitNodeDataJson extends RentalUnitModel {

    public enum UnitType {
        PROPERTY,
        SITE,
        BUILDING,
        APARTMENT,
        COMMERCIAL,
        GARAGE
    }

    @Schema(description = "Type of the node (e.g., 'PROPERTY', 'BUILDING')", required = true, examples = "PROPERTY")
    UnitType getType();

    @Override
    @Schema(description = "Title of the node", examples = "Main Building")
    String getTitle();

    @Override
    @Nullable
    @Schema(description = "Description of the node", examples = "A multi-story office building")
    String getDescription();

    @Override
    @Nullable
    @JsonIgnore
    TenancyModel getTenancy();

    @Nullable
    @Schema(description = "Name of the tenant associated with this node", examples = "Doe, John")
    String getTenant();

    @Override
    @Nullable
    @Schema(description = "Usable space in square meters", examples = "350.5")
    Float getUsableSpace();

}
