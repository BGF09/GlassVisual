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

    public LauncherForm()
    {
        Text = "NULL Launcher";
        Width = 590; Height = 370;
        FormBorderStyle = FormBorderStyle.FixedDialog;
        MaximizeBox = false;
        BackColor = Color.FromArgb(14, 16, 22);
        ForeColor = Color.White;
        Font = new Font("Segoe UI", 10);
        var title = new Label { Text = "NULL", Font = new Font("Segoe UI", 34, FontStyle.Bold), ForeColor = Color.FromArgb(130, 255, 190), AutoSize = true, Left = 32, Top = 22 };
        var subtitle = new Label { Text = "VISUAL CLIENT  /  MINECRAFT 1.21.4", AutoSize = true, Left = 38, Top = 91, ForeColor = Color.Silver };
        var install = MakeButton("УСТАНОВИТЬ / ОБНОВИТЬ NULL", 38, 145, 500, 48);
        install.Click += (_, _) => InstallMod();
        var launch = MakeButton("ОТКРЫТЬ MINECRAFT LAUNCHER", 38, 205, 500, 48);
        launch.BackColor = Color.FromArgb(45, 125, 90);
        launch.Click += (_, _) => OpenMinecraft();
        status.SetBounds(38, 274, 500, 42);
        status.ForeColor = Color.Gainsboro;
        status.Text = "Установи мод, затем выбери профиль Fabric 1.21.4 в официальном лаунчере.";
        Controls.AddRange([title, subtitle, install, launch, status]);
    }

    private Button MakeButton(string text, int x, int y, int w, int h)
    {
        var b = new Button { Text = text, Left = x, Top = y, Width = w, Height = h, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(42, 46, 56), ForeColor = Color.White, Cursor = Cursors.Hand };
        b.FlatAppearance.BorderSize = 0;
        return b;
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
            status.Text = "NULL установлен. Можно открывать официальный Minecraft Launcher.";
        }
        catch (Exception ex) { MessageBox.Show(ex.Message, "NULL Launcher", MessageBoxButtons.OK, MessageBoxIcon.Error); }
    }

    private void OpenMinecraft()
    {
        try
        {
            var candidates = new[]
            {
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "Minecraft Launcher", "Minecraft.exe"),
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "WindowsApps", "Microsoft.4297127D64EC6_8wekyb3d8bbwe", "Minecraft.exe")
            };
            var exe = candidates.FirstOrDefault(File.Exists);
            if (exe is not null) Process.Start(new ProcessStartInfo(exe) { UseShellExecute = true });
            else Process.Start(new ProcessStartInfo("minecraft://") { UseShellExecute = true });
            status.Text = "Открыл Minecraft Launcher. Выбери профиль Fabric 1.21.4.";
        }
        catch (Exception ex) { MessageBox.Show("Не удалось открыть Minecraft Launcher. Запусти его вручную.\n" + ex.Message, "NULL Launcher", MessageBoxButtons.OK, MessageBoxIcon.Warning); }
    }
}
