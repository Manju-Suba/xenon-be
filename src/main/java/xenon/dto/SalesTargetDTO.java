package xenon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xenon.model.SalesHierarchy;
import xenon.model.SalesmanTarget;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesTargetDTO {
    private SalesmanTarget saleTarget;
    private SalesHierarchy salesHierarchy;

}
