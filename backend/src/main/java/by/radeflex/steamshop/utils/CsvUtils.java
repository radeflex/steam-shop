package by.radeflex.steamshop.utils;

import by.radeflex.steamshop.dto.AccountCreateDto;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.MappingIterator;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@UtilityClass
public class CsvUtils {
    @SneakyThrows
    public static List<AccountCreateDto> readAccounts(MultipartFile csv, char sep) {
        Reader reader = new InputStreamReader(csv.getInputStream());
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(AccountCreateDto.class)
                .withColumnSeparator(sep).withSkipFirstDataRow(true);
        MappingIterator<AccountCreateDto> iterator = mapper
                .readerFor(AccountCreateDto.class)
                .with(schema)
                .readValues(reader);
        return iterator.readAll();
    }
}
