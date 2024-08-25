if exist target\test-classes\yo\dbunitcli\application\bat (
  rd /s /q target\test-classes\yo\dbunitcli\application\bat
)
mkdir target\test-classes\yo\dbunitcli\application\bat
copy core\src\test\resources\yo\dbunitcli\application\bat\expect\%copyTarget% core\target\test-classes\yo\dbunitcli\application\bat