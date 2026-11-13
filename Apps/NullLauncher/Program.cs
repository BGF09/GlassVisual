using System.Diagnostics;
using System.Reflection;

namespace NullLauncher;

internal static class Program
{
    [STAThread]
    static void Main()
    {
        ApplicationConfiguration.Initialize();
        Application.Run(new LauncherForm());
    }
}

internal sealed class LauncherForm : Form
{
    private readonly Label status = new();
    private readonly string minecraft = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), ".minecraft");
    private readonly string settingsDir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "NULL");
    private readonly string launcherPathFile;
    private readonly TextBox launcherPath = new();

    public LauncherForm()
    {
        launcherPathFile = Path.Combine(settingsDir, "legacy-launcher-path.txt");
        Text = "NULL Launcher";
        Width = 620; Height = 430;
        FormBorderStyle = FormBorderStyle.FixedDialog;
        MaximizeBox = false;
        BackColor = Color.FromArgb(14, 16, 22);
        ForeColor = Color.White;
        Font = new Font("Segoe UI", 10);
        var title = new Label { Text = "NULL", Font = new Font("Segoe UI", 34, FontStyle.Bold), ForeColor = Color.FromArgb(190, 130, 255), AutoSize = true, Left = 32, Top = 18 };
        var subtitle = new Label { Text = "VISUAL CLIENT  /  MINECRAFT 1.21.4", AutoSize = true, Left = 38, Top = 86, ForeColor = Color.Silver };
        var install = MakeButton("УСТАНОВИТЬ / ОБНОВИТЬ NULL", 38, 128, 530, 44);
        install.Click += (_, _) => InstallMod();
        var pathLabel = new Label { Text = "Путь к Legacy Launcher:", AutoSize = true, Left = 38, Top = 190, ForeColor = Color.Gainsboro };
        launcherPath.SetBounds(38, 216, 405, 32);
        launcherPath.Text = LoadLauncherPath() ?? FindLegacyLauncher() ?? "";
        var browse = MakeButton("ОБЗОР…", 453, 215, 115, 34);
        browse.Click += (_, _) => ChooseLegacyLauncher();
        var play = MakeButton("PLAY  /  ЗАПУСТИТЬ LEGACY LAUNCHER", 38, 270, 530, 48);
        play.BackColor = Color.FromArgb(85, 48, 135);
        play.Click += (_, _) => LaunchLegacy();
        status.SetBounds(38, 333, 530, 48);
        status.ForeColor = Color.Gainsboro;
        status.Text = "Укажи LegacyLauncher.exe. Авторизация и выбор профиля останутся в Legacy Launcher.";
        Controls.AddRange([title, subtitle, install, pathLabel, launcherPath, browse, play, status]);
    }

    private Button MakeButton(string text, int x, int y, int w, int h)
    {
        var b = new Button { Text = text, Left = x, Top = y, Width = w, Height = h, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(42, 46, 56), ForeColor = Color.White, Cursor = Cursors.Hand };
        b.FlatAppearance.BorderSize = 0;
        return b;
    }

    private string? LoadLauncherPath()
    {
        try { return File.Exists(launcherPathFile) ? File.ReadAllText(launcherPathFile).Trim() : null; }
        catch { return null; }
    }

    private string? FindLegacyLauncher()
    {
        var roots = new[]
        {
            Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles),
            Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86),
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData)
        };
        var candidates = roots.Where(x => !string.IsNullOrWhiteSpace(x)).SelectMany(root => new[]
        {
            Path.Combine(root, "Legacy Launcher", "LegacyLauncher.exe"),
            Path.Combine(root, "Legacy Launcher", "LegacyLauncher.exe"),
            Path.Combine(root, "LegacyLauncher", "LegacyLauncher.exe"),
            Path.Combine(root, "Programs", "Legacy Launcher", "LegacyLauncher.exe")
        });
        return candidates.FirstOrDefault(File.Exists);
    }

    private void ChooseLegacyLauncher()
    {
        using var dialog = new OpenFileDialog { Title = "Выбери исполняемый файл Legacy Launcher", Filter = "Legacy Launcher (*.exe)|*.exe|Все файлы (*.*)|*.*" };
        if (dialog.ShowDialog() != DialogResult.OK) return;
        launcherPath.Text = dialog.FileName;
        try
        {
            Directory.CreateDirectory(settingsDir);
            File.WriteAllText(launcherPathFile, dialog.FileName);
            status.Text = "Путь к Legacy Launcher сохранён локально.";
        }
        catch (Exception ex) { MessageBox.Show(ex.Message, "NULL Launcher", MessageBoxButtons.OK, MessageBoxIcon.Warning); }
    }

    private void InstallMod()
    {
        try
        {
            var mods = Path.Combine(minecraft, "mods");
            Directory.CreateDirectory(mods);
            using var source = Assembly.GetExecutingAssembly().GetManifestResourceStream("NULL.jar")
                ?? throw new FileNotFoundException("В сборку не встроен NULL.jar.");
            using var target = File.Create(Path.Combine(mods, "NULL-Visual-1.0.0.jar"));
            source.CopyTo(target);
            status.Text = "NULL установлен в .minecraft\\mods.";
        }
        catch (Exception ex) { MessageBox.Show(ex.Message, "NULL Launcher", MessageBoxButtons.OK, MessageBoxIcon.Error); }
    }

    private void LaunchLegacy()
    {
        try
        {
            var exe = launcherPath.Text.Trim().Trim('"');
            if (string.IsNullOrWhiteSpace(exe) || !File.Exists(exe))
            {
                MessageBox.Show("Не найден Legacy Launcher. Нажми ОБЗОР и выбери его .exe файл.", "NULL Launcher", MessageBoxButtons.OK, MessageBoxIcon.Information);
                return;
            }
            Directory.CreateDirectory(settingsDir);
            File.WriteAllText(launcherPathFile, exe);
            Process.Start(new ProcessStartInfo(exe) { UseShellExecute = true, WorkingDirectory = Path.GetDirectoryName(exe)! });
            status.Text = "Legacy Launcher запущен. Выбери Fabric 1.21.4 и авторизуйся в нём.";
        }
        catch (Exception ex) { MessageBox.Show("Не удалось запустить Legacy Launcher.\\n" + ex.Message, "NULL Launcher", MessageBoxButtons.OK, MessageBoxIcon.Error); }
    }
}
