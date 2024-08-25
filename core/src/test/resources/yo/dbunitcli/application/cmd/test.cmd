if exist target\test-classes\yo\dbunitcli\application\cmd (
  rd /s /q target\test-classes\yo\dbunitcli\application\cmd
)
mkdir target\test-classes\yo\dbunitcli\application\cmd
copy src\test\resources\yo\dbunitcli\application\cmd\expect\%copyTarget% target\test-classes\yo\dbunitcli\application\cmd