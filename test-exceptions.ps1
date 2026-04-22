param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

function Get-ErrorBody {
    param(
        [Parameter(Mandatory = $true)]
        $ErrorRecord
    )

    if ($ErrorRecord.ErrorDetails -and $ErrorRecord.ErrorDetails.Message) {
        return $ErrorRecord.ErrorDetails.Message
    }

    if ($ErrorRecord.Exception.Response) {
        try {
            $stream = $ErrorRecord.Exception.Response.GetResponseStream()
            if ($stream) {
                $reader = New-Object System.IO.StreamReader($stream)
                $body = $reader.ReadToEnd()
                $reader.Close()
                if (-not [string]::IsNullOrWhiteSpace($body)) {
                    return $body
                }
            }
        } catch {
        }
    }

    return "(no response body captured)"
}

$tests = @(
    @{
        Name = "PROJECT_NOT_FOUND"
        Method = "GET"
        Url = "$BaseUrl/api/projects/PRJ-DOES-NOT-EXIST"
    },
    @{
        Name = "TASK_NOT_FOUND"
        Method = "PATCH"
        Url = "$BaseUrl/api/tasks/TSK-DOES-NOT-EXIST"
        Body = '{"status":"IN_PROGRESS"}'
    },
    @{
        Name = "TASK_ALREADY_COMPLETED"
        Method = "PATCH"
        Url = "$BaseUrl/api/tasks/TSK-EV-01"
        Body = '{"status":"COMPLETED"}'
    },
    @{
        Name = "INVALID_TASK_DATES"
        Method = "POST"
        Url = "$BaseUrl/api/tasks"
        Body = '{"projectId":"PRJ-EV-LINE","name":"Bad Task","description":"Invalid date check","startDate":"2026-10-01","dueDate":"2026-08-01","status":"PLANNED","priority":"HIGH","assignedTo":"Rohit Anand"}'
    },
    @{
        Name = "RESOURCE_NOT_AVAILABLE"
        Method = "POST"
        Url = "$BaseUrl/api/tasks"
        Body = '{"projectId":"PRJ-ENG-OPT","name":"Maintenance Conflict","description":"Unavailable resource check","startDate":"2026-06-11","dueDate":"2026-06-20","status":"PLANNED","priority":"HIGH","assignedTo":"Abhishek Nair"}'
    },
    @{
        Name = "RESOURCE_NOT_FOUND"
        Method = "PATCH"
        Url = "$BaseUrl/api/resources/RES-DOES-NOT-EXIST"
        Body = '{"role":"Project Engineer"}'
    },
    @{
        Name = "MILESTONE_NOT_FOUND"
        Method = "PATCH"
        Url = "$BaseUrl/api/milestones/MLS-DOES-NOT-EXIST"
        Body = '{"completionStatus":true}'
    },
    @{
        Name = "EXPENSE_NOT_FOUND"
        Method = "PATCH"
        Url = "$BaseUrl/api/expenses/EXP-DOES-NOT-EXIST"
        Body = '{"amount":250000}'
    },
    @{
        Name = "INVALID_PROJECT_DATES"
        Method = "POST"
        Url = "$BaseUrl/api/projects"
        Body = '{"name":"Invalid Date Program","description":"Project date validation check","managerName":"Demo Manager","startDate":"2026-12-01","endDate":"2026-10-01","status":"PLANNED","objectives":"Should fail because startDate is after endDate","progressPercent":0,"budgetTotal":1000000,"budgetSpent":0}'
    },
    @{
        Name = "PROJECT_REPORT_NOT_FOUND"
        Method = "GET"
        Url = "$BaseUrl/api/projects/PRJ-DOES-NOT-EXIST/report"
    }
)

foreach ($test in $tests) {
    Write-Host ""
    Write-Host "=== $($test.Name) ===" -ForegroundColor Cyan
    try {
        $params = @{
            Uri = $test.Url
            Method = $test.Method
            UseBasicParsing = $true
        }
        if ($test.ContainsKey("Body")) {
            $params["ContentType"] = "application/json"
            $params["Body"] = $test.Body
        }

        $response = Invoke-WebRequest @params
        Write-Host "Unexpected success" -ForegroundColor Yellow
        Write-Host "Status: $($response.StatusCode)"
        Write-Host "Body: $($response.Content)"
    } catch {
        $statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.value__ } else { "N/A" }
        $body = Get-ErrorBody -ErrorRecord $_

        Write-Host "Status: $statusCode"
        Write-Host "Body: $body"
    }
}

Write-Host ""
Write-Host "Done. Check server-out.log and server-err.log if you want to verify logging too." -ForegroundColor Green
