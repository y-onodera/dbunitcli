if exist core\target\test-classes\yo\dbunitcli\application\bat (
  rd /s /q core\target\test-classes\yo\dbunitcli\application\bat
)
mkdir core\target\test-classes\yo\dbunitcli\application\bat
copy core\src\test\resources\yo\dbunitcli\application\expect\bat\expect\%copyTarget% core\target\test-classes\yo\dbunitcli\application\bat