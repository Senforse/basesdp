---
name: utf8-encoding
description: 保证 SDP 项目所有源文件为 UTF-8 无 BOM 编码，修复 Windows 下 Cursor 写文件偶发的 UTF-16 LE / BOM 导致的中文乱码、Java 编译失败、达梦中文存储异常。在 Windows 上新建或修改含中文的文件（.java/.md/.sql/.vue/.ts/.yml 等）后、或看到乱码/编码报错时使用。
---

# UTF-8 无 BOM 编码规范

SDP 含大量中文（注释、文档、达梦 SQL、Vue 文案）。在 Windows + PowerShell 环境下，Cursor 的 `Write` 偶发把文件存成 **UTF-16 LE**（字节如 `70 00 61 00`）或带 BOM，导致 Java 编译报错、Git diff 全红、达梦中文乱码。本项目统一约定：**所有源文件必须是 UTF-8 无 BOM**。

## 核心规则

- 新建/重写**含中文**的文件后，**主动运行修复脚本**校正编码，不要假设 `Write` 已写对。
- 纯 ASCII 文件一般无需处理，但批量改动后顺手校验无妨。
- 不要用 `echo >`、`Out-File`（默认可能带 BOM 或 UTF-16）创建源文件；用 `Write` 工具，再跑修复脚本。

## 修复脚本（首选）

项目已内置 `scripts/ensure-utf8-no-bom.ps1`，能识别并就地转换：UTF-8 BOM、UTF-16 LE(含/不含 BOM)、UTF-16 BE BOM → UTF-8 无 BOM。**优先用它，不要重新发明。**

单个文件：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/ensure-utf8-no-bom.ps1 -Path 'back-end\sdp-admin\...\SysMenu.java'
```

多个文件（一次传入数组）：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/ensure-utf8-no-bom.ps1 -Path 'a.java','b.sql','c.md'
```

脚本只重写"需要修复"的文件，安全幂等；正常 UTF-8 文件不动。

## 排查乱码

- **Java 编译报"非法字符 / 编码 GBK 不可映射"**：源文件多半是 UTF-16/BOM，跑修复脚本。
- **Git diff 整文件全变更**：编码被改写，跑脚本恢复 UTF-8 无 BOM。
- **达梦库中文变 `???` 或乱码**：先确认 SQL 文件本身是 UTF-8 无 BOM，再确认连接 URL/客户端字符集；文件编码是第一嫌疑。
- **判断字节**：用 `Read` 看不出编码，可 `Format-Hex -Path file -Count 8` 看头部字节（`FF FE`=UTF-16LE BOM；`EF BB BF`=UTF-8 BOM；`xx 00 xx 00`=UTF-16LE 无 BOM）。

## 另有 Python 工具

`back-end/sdp-admin/normalize_text_encoding.py` 用于该后端目录的批量规范化（配合 `materialize_sdp_design_md.py` 处理设计文档）。处理后端文档/SQL 批量编码时可参考，单文件修复仍优先用根目录的 PowerShell 脚本。
